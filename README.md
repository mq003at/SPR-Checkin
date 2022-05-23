# Welcome to SPR-Checkin

SPR-Checkin is an APK written in Java. It provides an easy way to get access to SPR Empployee Database and primarily be used as a self-check in for the employees. It is designed to be easily used, and implemented afterwards. 
The APK requires Android version 7.0 or above.


## Installation
### 1. Using APK file
* Navigate to **Release** section on the right side of this GitHub.
* Download the .apk file of the latest version to your Android Phone.
* (If the APK is not installed) Enable Unknown Sources on your Android Phone by going to **Settings -> Install Unknown Apps -> Install Unknown Apps**.

### 2. Building from source codes.
* You need Android Studio or other IDEs that can compile Gradle project (IntelliJ, VSCode, etc.). For this guide, I will guide you how to build using Android Studio.
* After downloading the source codes, create a new project in Android Studio. Then copy the project files into the project foler.
* From the Tool bar on top, select **Build -> Build Bundles / APK -> Build APKs**.
* After Gradle finish compiling, you can **Locate** the APK file from **Event Log** on the bottom-right corner of Android Studio. Copy it to your phone to install and run it.
* (Optional) You can pair your phone over Wifi to run the APK. From the Debug Device Selection on the toolbar, choose **Pair Devices over Wifi.** The APK will be built and installed directly through Wifi.
* **NOTE:** You can run the APK from your IDE's emulator by pressing **Shift + F10**. However, since the APK requires NFC scanner to work, the emulator will not work as intentded.


##  Usage

To use the APK, put an NFC-tag behind the phone. NFC-scanner will read it and check the database to show the name of the employee. Then you can press the button to check in or check out from the database. Info logs will also display on the UI.

### Main Activity
Contains 2 placeholders to output data or error logs. There is a button to trigger database function and a function to refresh the employee Object.

### Firebase Access
Firebase functions must be placed here. Currently there are *update_text* and *add_record* to edit the records from database. For other *GET* functions:

- *self_check*: Check the current status of employee and change their state ("in" to "out" and reverse), then, add a new log record to the database.
- *get_name*: Check the database to get the name from the NFC tag.
- *generate_logs*: Output all records made on current day to *.txt* file. You can access the file in your phone's *Document* directory.
- *logout_all*: Check today employees' state. If they are still "in", change to "out" and output the warning. Manager can use this warning to manually add the record from SPR site.

### Database Object
Currently there are two database Objects (Employee and EventLog). You can use these to manipulate corresponding collection on Firebase.

### IOStream *(currently not used)*

IO functions to write and read external files. Currently you can use *write_to_file* to output data to *.txt* file.

### LocaleHelper
Stores function to change the locale of the app. Currently there are 3 supported locale: English (us), Finnish (fi) and Swedish (sv). To change your Locale add this to your function.

    sharedPreferences.edit().putString("locale", YOUR_LOCALE).apply();  
    LocaleHelper.setLocale(this, YOUR_LOCALE);  
    Intent intent = getIntent();  
    finish();  
    startActivity(intent);

New locale can be added by adding new *strings.xml* file in *value* directory (if you use Android Studio, you can use Translation Tools).

### Task Manager
Stores tasks to run automatically. Currently, there is only the task to automtically logout employees using *logout_schedule*. You can turn this off on your conditions by using *unregister_schedule*. More task functions must be made here.

### Database
The databse config file is stored in *google-services.json*. If you want to reference to a different database, simple replace this file.

## Final Words
Although the project is open-source, the database belongs to SPR-Kirppis so please only make modifications depending on their requests. If you want to add something, do not hesitate to create a pull request. 
