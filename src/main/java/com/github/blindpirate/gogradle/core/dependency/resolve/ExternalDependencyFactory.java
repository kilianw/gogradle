package com.github.blindpirate.gogradle.core.dependency.resolve;

import com.github.blindpirate.gogradle.core.GolangPackageModule;
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.util.Assert;

import java.util.Optional;

import java.nio.file.Path;
import java.util.List;

public abstract class ExternalDependencyFactory implements DependencyFactory {

    /**
     * Relative paths of identity files.
     * For example, "Godeps/Godeps.json","glide.yaml"
     *
     * @return
     */
    protected abstract List<String> identityFiles();

    @Override
    public Optional<GolangDependencySet> produce(GolangPackageModule module) {
        if (anyFileExist(module)) {
            return doProduce(module);
        } else {
            return Optional.empty();
        }
    }

    protected abstract Optional<GolangDependencySet> doProduce(GolangPackageModule module);

    private boolean anyFileExist(GolangPackageModule module) {
        List<String> identityFiles = identityFiles();
        Assert.isNotEmpty(identityFiles, "Identity files must not be empty!");
        Path rootDir = module.getRootDir();
        return identityFiles.stream()
                .anyMatch(file -> rootDir.resolve(file).toFile().exists());
    }
}
