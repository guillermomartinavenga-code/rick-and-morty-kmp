// swift-tools-version:5.9
import PackageDescription

let package = Package(
    name: "ComposeAppPackage",
    platforms: [.iOS(.v14)],
    products: [
        .library(name: "ComposeApp", targets: ["ComposeApp"])
    ],
    targets: [
        .binaryTarget(
            name: "ComposeApp",
            path: "../composeApp/build/XCFrameworks/debug/ComposeApp.xcframework"
        )
    ]
)
