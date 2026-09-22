//
//  ContentView.swift
//  iosApp
//
//  Created by Guillermo Oscar Martin on 18/09/2026.
//

import ComposeApp
import SwiftUI

struct ContentView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }
    
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

#Preview {
    ContentView()
}
