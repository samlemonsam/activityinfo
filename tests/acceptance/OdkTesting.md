
# Running ODK Integration Tests Locally

To run the ActivityInfo-ODK integration tests locally, you will need to setup Appium on your local machine.

The general instructions are here:
https://github.com/appium/appium/blob/master/docs/en/appium-setup/android-setup.md

The following are a set of steps that actually worked on my Linux box:

## Download and extract SDK

Download and extract the [http://dl.google.com/android/android-sdk_r23.0.2-linux.tgz](Android SDK) to somewhere
in your home directory. Extracting as `root` leads to problems.

## Set the `ANDROID_HOME` environment variable:

    export ANDROID_HOME=/home/alex/dev/android-sdk-linux


## Configure SDK

Run the `android` GUI tool

    $ANDROID_HOME/tools/android

and select

  * "Android 4.3 (API 19)" and
  * "Android SDK Build-tools 
  
from the tree, and click "Install ... package(s)"

N.B. Installing API 20 didn't work for me.

## Checkout and build the Appium source:


    git clone git@github.com:appium/appium.git
    cd appium
    git checkout v1.4.3$AN

    export JAVA_HOME=/usr/lib/jvm/
    export MAVEN_OPTS="-Xms512m -Xmx512m -Xss2096k"
    ./reset.sh --android --selendroid --verbose

## Connect your Android Device

Connect your Android Handset via USB and enable "Developer mode".

From the command line, verify that the device has been recognized:

    $ANDROID_HOME/platform-tools/adb devices

You should see something resembling:

    List of devices attached
    LGOTMS71fd129c  device

## Start the Appium Server

(From the Appum Source dir)

    node .