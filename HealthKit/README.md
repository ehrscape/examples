#HKConnect

Access your data from Apple HealthKit in JSON or CSV format and combine it with your existing health data.

##Requirements

* Xcode 6
* LLVM 6
* iOS SDK 8.0
* Ruby
* CocoaPods

##Instructions

Before you continue building the project, make sure you have updated Xcode to the latest version.

The project itself requires the CocoaPods dependency management software. To install it, just run the following command in the terminal:

	sudo gem install cocoapods

For more info, please consult the official [Getting Started](http://guides.cocoapods.org/using/getting-started.html) guide.

After you have set up CocoaPods, navigate to the HKConnect directory and run:

	pod install

When the `pod` has finished, open the project using the workspace file `HKConnect.xcworkspace` and run it in the simulator or on an actual device.

##Note

The project uses the **git flow** branching model, so please check out the `develop` branch.