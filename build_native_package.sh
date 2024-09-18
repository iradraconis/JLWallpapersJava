#!/bin/bash

# Step 1: Build the jar (with dependencies)

# Step 2:
# Install Packager tools for Linux (RHEL)
# sudo dnf install fedora-packager fedora-review

# Linux: signing - erstelle oder importiere einen GPG-Schlüssel
# Ggfs. einen neuen GPG-Schlüssel erstellen: (nur RSA num Signieren)

# gpg --full-generate-key
# gpg --list-keys


# Step 3: Build the package
# MacOs 
# check if os is windows, linux, mac

if [[ "$OSTYPE" == "linux-gnu"* ]]; then
    echo "Build für Linux gestartet..."
    # native package for linux corresponding to the current OS
    # RPM Package
    jpackage --name jL-Wallpapers --linux-package-name jL-Wallpapers --input ./target/ --main-jar jL-Wallpapers-0.1.jar --main-class com.iradraconis.jl.wallpapers.JLWallpapers --icon ./src/main/resources/icon.png --type rpm --resource-dir ./src/main/resources/ --verbose
elif [[ "$OSTYPE" == "darwin"* ]]; then
    echo "Build für MacOS gestartet..."
    jpackage --name jL-Wallpapers --input ./target/ --main-jar jL-Wallpapers-0.1.jar --main-class com.iradraconis.jl.wallpapers.JLWallpapers --icon ./src/main/resources/icon.icns
else
    echo "Build für Windows gestartet..."
    # MSI Package
    jpackage --name jL-Wallpapers --input ./target/ --main-jar jL-Wallpapers-0.1.jar --main-class com.iradraconis.jl.wallpapers.JLWallpapers --icon ./src/main/resources/icon.ico --type msi
fi

# Step 4: Install the package

# Step 5: Run the application

# Step 6: Uninstall the package
# dnf list installed | grep jlsync

# Deinstallieren - Fedora:
# sudo dnf remove jlsync