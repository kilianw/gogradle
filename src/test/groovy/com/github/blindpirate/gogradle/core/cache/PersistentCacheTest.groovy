package com.github.blindpirate.gogradle.core.cache

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.GolangCloneable
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.IOUtils
import com.github.blindpirate.gogradle.util.ReflectionUtils
import org.gradle.api.Project
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito

import java.util.concurrent.ConcurrentHashMap

import static com.github.blindpirate.gogradle.core.cache.AbstractCacheTest.GolangCloneableForTest

@RunWith(GogradleRunner)
@WithResource('')
class PersistentCacheTest {
    File resource

    @Mock
    Project project

    PersistentCache cache

    File storageFile

    @Before
    void setUp() {
        cache = new PersistentCacheForTest()
        storageFile = new File(resource, ".gogradle/cache/${PersistentCacheForTest.simpleName}.bin")
        Mockito.when(project.getRootDir()).thenReturn(resource)
    }

    Map prepareCacheMap() {
        ConcurrentHashMap map = new ConcurrentHashMap()
        map[buildCloneable(1)] = buildCloneable(1)
        map[buildCloneable(2)] = buildCloneable(2)
        return map
    }

    @Test
    void 'loading from serialization file should succeed'() {
        IOUtils.serialize(prepareCacheMap(), storageFile)
        cache.load()
        assert cache.get(buildCloneable(1), null) == buildCloneable(1)
        assert cache.get(buildCloneable(2), null) == buildCloneable(2)
    }

    @Test
    void 'nothing should happen if serialization does not exist'() {
        cache.load()
        assert ReflectionUtils.getField(cache, 'container').isEmpty()
    }

    @Test
    void 'nothing should happen if exception occurs in deserialization'() {
        IOUtils.write(storageFile, '')
        cache.load()
        assert ReflectionUtils.getField(cache, 'container').isEmpty()
    }

    @Test
    void 'only hit cache should be persisted'() {
        // given
        ReflectionUtils.setField(cache, 'container', prepareCacheMap())
        // when
        assert cache.get(buildCloneable(1), null) == buildCloneable(1)
        cache.save()
        cache.load()
        // then
        assert ReflectionUtils.getField(cache, 'container').size() == 1
        assert cache.get(buildCloneable(1), null) == buildCloneable(1)
    }

    GolangCloneable buildCloneable(int value) {
        return new GolangCloneableForTest(value: value);
    }

    class PersistentCacheForTest extends PersistentCache<GolangCloneableForTest, GolangCloneableForTest> {
        PersistentCacheForTest() {
            super(PersistentCacheTest.this.project)
        }
    }
}