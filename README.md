# Extract Android Manifest File - Android
Android app to extract manifest file from apk

This android application (Java) extracts the AndroidManifest.xml from a given apk file and stores the content of the extracted file in manifest.xml using [apktool library.](https://github.com/iBotPeaches/Apktool)

> I have added the jar file as a library to the application. If you need an updated version of the library Go to: https://bitbucket.org/iBotPeaches/apktool/downloads/ , [place the jar file in the libs folder and add as library.](https://stackoverflow.com/questions/25660166/how-to-add-a-jar-in-external-libraries-in-android-studio)<br/>

### Potential issues and how to solve them.
> Please take a look at: https://github.com/iBotPeaches/Apktool/issues/3036 and https://platinmods.com/threads/how-to-fix-apktool-decompile-error-using-mt-manager-app-arscdecoder-error.121708/ if a specific apk does not get decoded properly.

> If your gradle throws an error while building, such as `The project is using an incompatible version (AGP 7.3.0-alpha07) of the Android Gradle plugin. Latest supported version is AGP 7.2.1`. Please update the AGP version to the latest supported one in `build.gradle (Project level)` <img width="799" alt="image" src="https://github.com/YashsviG/extract-manifest-file/assets/45160510/590fb4bf-3bd8-4618-8289-0b3385d9875a"> <br/> and `gradle-wrapper.properties` <img width="764" alt="image" src="https://github.com/YashsviG/extract-manifest-file/assets/45160510/426032f0-361d-4b8e-bb18-481a4cdf1aa1">



### Sample Demo
I have tested this app with Adobe reader apk, here is a sample video of how to run and what to expect

https://github.com/YashsviG/extract-manifest-file/assets/45160510/0bfd95ac-1864-4956-8fbf-cf155c5cc83d

