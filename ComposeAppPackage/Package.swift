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
//             path: "../composeApp/build/XCFrameworks/debug/ComposeApp.xcframework"
            url: "https://github.com/guillermomartinavenga-code/rick-and-morty-kmp/releases/download/v0.1.0/ComposeApp.xcframework.zip",
            checksum: "280f7c0d3e51383b1a0eab7bfbb5defda29c9907faf8b0e85c1b51ab06a266cb"
        )
    ]
)
