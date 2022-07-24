mkdir .\jpackage\
cp .\target\launcher-bootstrapper-$args.jar .\jpackage\launcher-bootstrapper-$args.jar
jpackage --input ./jpackage --name 'Zavar Launcher 2' --main-jar launcher-bootstrapper-$args.jar --main-class com.zavar.bootstrapper.BootstrapperJar --type exe --win-shortcut --win-menu --app-version $args --icon icon.ico --vendor Zavar30 --dest ./target
rmdir .\jpackage\ -r