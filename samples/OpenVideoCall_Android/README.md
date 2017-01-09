## NOTICE

OpenVideoCall is a demo of [Agora](http://www.agora.io) Video SDK - Android


## Bug reports

* https://github.com/AgoraLab/OpenVideoCall_Android/issues


## Build Instructions

Java 8 SDK/Android SDK/NDK tools need to be ready on you host machine, if you does not have them ready, follow instructions here:

* http://www.oracle.com/technetwork/java/javase/overview/index.html
* https://developer.android.com/studio/index.html
* https://developer.android.com/ndk/index.html


We use `Gradle`/`Jack and Jill` to build, if you want know more about `Gradle`/`Jack and Jill`, follow instructions here:

* http://tools.android.com/tech-docs/jackandjill
* https://developer.android.com/studio/build/index.html
* http://gradle.org/getting-started-android-build/



NOTICE: before building, you need to


1. update your App ID at app/src/main/res/values/strings_config.xml

	private_app_id

	you can get your own ID at https://dashboard.agora.io/


2. If you get source code directly from GitHub, you need to copy library and header files from Agora SDK package to your project, follow below instructions.

	update libraries at app/libs(*.jar) and app/src/main/libs(*.so), update header files at app/src/main/cpp/agora, check PLACEHOLDER for details

	If you get source code from Agora SDK package, we have already configured it well, just build and run it.


Gradle build instructions

	./gradlew assembleDebug
This will generate the APK, you need to install and run this APK on Android devices.

Or just use the one step command to build and install

	./gradlew installDebug


Enjoy video calling
