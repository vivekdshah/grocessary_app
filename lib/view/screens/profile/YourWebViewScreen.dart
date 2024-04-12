
import 'dart:collection';

import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter_inappwebview/flutter_inappwebview.dart';

class MyInAppBrowser extends InAppBrowser {
  final BuildContext context;
  late Function(bool) onCloseMessage;
  bool isComplete = false;

  MyInAppBrowser(this.context, {
  required this.onCloseMessage,
    int? windowId,
    UnmodifiableListView<UserScript>? initialUserScripts,
  })
      : super(windowId: windowId, initialUserScripts: initialUserScripts);

  @override
  Future onBrowserCreated() async {
    debugPrint("\n\nBrowser Created!\n\n");
  }

  @override
  Future onLoadStart(url) async {
    debugPrint("\n\nStarted: $url\n\n");
    _pageRedirect(url.toString());
  }

  @override
  Future onLoadStop(url) async {
    // Handle onLoadStop logic...
  }

  @override
  void onExit() {
    // Handle onExit logic...
    if (!isComplete) {
      onCloseMessage(false);
    }
  }

  @override
  Future<NavigationActionPolicy> shouldOverrideUrlLoading(navigationAction) async {
    debugPrint("\n\nOverride ${navigationAction.request.url}\n\n");
    return NavigationActionPolicy.ALLOW;
  }

  @override
  void onLoadResource(resource) {
    // Handle onLoadResource logic...

  }

  @override
  void onConsoleMessage(consoleMessage) {
    debugPrint("""
    WebView console output:
      message: ${consoleMessage.message}
      messageLevel: ${consoleMessage.messageLevel.toValue()}
   """);
  }

  void _pageRedirect(String url) {
    // Handle Stripe callback URL logic...
    _fetchContentAndHandle(url);
    debugPrint(url);
  }

  Future<void> _fetchContentAndHandle(String url) async {
    // Perform a network request to fetch the content of the URL
    if (url.contains('stripe_account_status')) {
      if (url.contains('status=0')) {
        onCloseMessage(false);
      } else {
        onCloseMessage(true);
      }
      isComplete = true;
      close();
    } 
  }

}