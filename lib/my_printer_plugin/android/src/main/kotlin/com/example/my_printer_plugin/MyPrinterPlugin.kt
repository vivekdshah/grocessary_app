package com.example.my_printer_plugin

import android.util.Log
import androidx.annotation.NonNull
import android.content.Context
import android.widget.Toast
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import com.starmicronics.stario10.InterfaceType
import com.starmicronics.stario10.StarConnectionSettings
import com.starmicronics.stario10.StarPrinter
import com.starmicronics.stario10.starxpandcommand.DocumentBuilder
import com.starmicronics.stario10.starxpandcommand.MagnificationParameter
import com.starmicronics.stario10.starxpandcommand.PrinterBuilder
import com.starmicronics.stario10.starxpandcommand.StarXpandCommandBuilder
import com.starmicronics.stario10.starxpandcommand.printer.Alignment
import com.starmicronics.stario10.starxpandcommand.printer.BarcodeParameter
import com.starmicronics.stario10.starxpandcommand.printer.BarcodeSymbology
import com.starmicronics.stario10.starxpandcommand.printer.CutType
import com.starmicronics.stario10.starxpandcommand.printer.ImageParameter
import com.starmicronics.stario10.starxpandcommand.printer.InternationalCharacterType
import com.starmicronics.stario10.starxpandcommand.printer.QRCodeLevel
import com.starmicronics.stario10.starxpandcommand.printer.QRCodeParameter
import android.graphics.BitmapFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import android.os.Build
import android.Manifest
import android.content.pm.PackageManager
import android.content.Context.MODE_PRIVATE

/** MyPrinterPlugin */
class MyPrinterPlugin: FlutterPlugin, MethodCallHandler {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private lateinit var channel : MethodChannel
  private lateinit var context: Context

  override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "samples.flutter.dev/MyChannel")
    channel.setMethodCallHandler(this)
    this.context = flutterPluginBinding.applicationContext
  }

  override fun onMethodCall(call: MethodCall, result: Result) {
    if (call.method == "getPlatformVersion") {
      result.success("Android ${android.os.Build.VERSION.RELEASE}")
    } else if (call.method == "print_receipt") {
      android.util.Log.i("Hello", "I am inside native_from_plugin")
      // Get the data from the Flutter method call
      val data = call.argument<Map<String, Any>>("data")
      // Check if the data is not null
      if (data != null) {
        // Convert the data to the appropriate Kotlin data class
        val converterClass = DataConverter()
        val orderDetailsModel = converterClass.convertToOrderDetailsModel(data)
        // Now you can use the orderDetailsModel as needed
        // For example, pass it to your print logic
        showMessage("Preparing printer")
        onPressPrintButton(orderDetailsModel)
      } else {
        showMessage("Receipt not found")
      }

      result.success("")
    }
    else {
      result.notImplemented()
    }
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }

  private fun showMessage(message: String) {
    android.util.Log.i("PRINTER_MESSAGE_FROM_PLUGIN", message)
      Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
  }

  private fun onPressPrintButton(orderDetailsModel: OrderDetailsModel) {
    val identifier = getPrinterIdentifier()
    val interfaceTypeName = getPrinterInterface()
    if (identifier.isBlank() || interfaceTypeName.isBlank()) {
      showMessage("Please setup printer")
      return
    }
    showMessage("Printing started...")
    val interfaceType = when (interfaceTypeName.lowercase()) {
      "LAN".lowercase() -> InterfaceType.Lan
      "Bluetooth".lowercase() -> InterfaceType.Bluetooth
      "USB".lowercase() -> InterfaceType.Usb
      else -> {
        showMessage("Printer interface not matched")
        return
      }
    }

    val settings = StarConnectionSettings(interfaceType, identifier)
    val printer = StarPrinter(settings, context)

    // If you are using Android 12 and targetSdkVersion is 31 or later,
    // you have to request Bluetooth permission (Nearby devices permission) to use the Bluetooth printer.
    // https://developer.android.com/about/versions/12/features/bluetooth-permissions
    if (interfaceType == InterfaceType.Bluetooth || settings.autoSwitchInterface) {
      if (!hasBluetoothPermission()) {
        showMessage("Bluetooth PERMISSION ERROR")
        Log.d(
          "Printing",
          "PERMISSION ERROR: You have to allow Nearby devices to use the Bluetooth printer."
        )
        return
      }
    }

    val job = SupervisorJob()
    val scope = CoroutineScope(Dispatchers.Default + job)

    scope.launch {
      try {
        // TSP100III series and TSP100IIU+ do not support actionPrintText because these products are graphics-only printers.
        // Please use the actionPrintImage method to create printing data for these products.
        // For other available methods, please also refer to "Supported Model" of each method.
        // https://star-m.jp/products/s_print/sdk/starxpand/manual/ja/android-kotlin-api-reference/stario10-star-xpand-command/printer-builder/action-print-image.html
        val builder = StarXpandCommandBuilder()
        builder.addDocument(
          DocumentBuilder()
            // To open a cash drawer, comment out the following code.
            .addPrinter(getPrintBuilder(orderDetailsModel))
        )
        val commands = builder.getCommands()

        printer.openAsync().await()
        printer.printAsync(commands).await()
        showMessage("Printing...")
        Log.d("Printing", "Success")
      } catch (e: Exception) {
        showMessage("Error ${e.localizedMessage}")
        Log.d("Printing", "Error: ${e}")
      } finally {
        printer.closeAsync().await()
      }
    }
  }

  private fun hasBluetoothPermission(): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
      return true
    }

    return true
  }

  private fun getPrinterIdentifier(): String {
    val prefs = context.getSharedPreferences("PrinterPrefs", MODE_PRIVATE)
    return prefs.getString("printerIdentifier", "") ?: ""
  }

  private fun getPrinterInterface(): String {
    val prefs = context.getSharedPreferences("PrinterPrefs", MODE_PRIVATE)
    return prefs.getString("printerInterfaceType", "") ?: ""
  }

  private fun calculateSubtotal(details: List<Details>): String {
    val subtotal = details.sumByDouble { it.price ?: 0.0 }
    return "%.2f".format(subtotal)
  }

  private fun calculateTotalDiscount(details: List<Details>): String {
    val totalDiscount = details.sumByDouble { it.discountOnProduct ?: 0.0 }
    return "%.2f".format(totalDiscount)
  }

  private fun calculateGrandTotal(orderDetailsModel: OrderDetailsModel): String {
    val subtotal = calculateSubtotal(orderDetailsModel.details).toDouble()
    val discount = calculateTotalDiscount(orderDetailsModel.details).toDouble()
    val tax = orderDetailsModel.order.tax
    val grandTotal = subtotal - discount + (tax ?: 0.0)

    // Format to two decimal places
    return "%.2f".format(grandTotal)
  }

  private fun getPrintBuilder(orderDetailsModel: OrderDetailsModel): PrinterBuilder {
    val builder = PrinterBuilder()
      .styleAlignment(Alignment.Center)
      .add(
        PrinterBuilder()
          .styleMagnification(MagnificationParameter(3, 4))
          .actionPrintText("Cresskill\n")
      )
      .add(
        PrinterBuilder()
          .styleMagnification(MagnificationParameter(2, 2))
          .actionPrintText("BAGEL CAFE\n\n\n")
      )
      .styleAlignment(Alignment.Left)
      .actionPrintText(
        "${orderDetailsModel.order.createdAt?.formatDateTime()}" +
                "\n----------------------------------------------\n"
      )
      .add(
        PrinterBuilder()
          .styleBold(true)
          .styleMagnification(MagnificationParameter(1, 1))
          .actionPrintText("Ready By:")
      )
      .add(
        PrinterBuilder()
          .styleBold(true)
          .actionPrintText("\nDate: ${orderDetailsModel.order.deliveryDate?.formatDate()}  Time: ${orderDetailsModel.order.deliveryTime?.formatTime()}")
      )
      .actionPrintText("\n----------------------------------------------\n\n")
//            .styleInternationalCharacter(InternationalCharacterType.Usa)
//            .styleCharacterSpace(0.0)
//            .styleAlignment(Alignment.Right)
//            .actionPrintText(
//                "Star Clothing Boutique\n" +
//                        "123 Star Road\n" +
//                        "City, State 12345\n" +
//                        "\n"
//            )

    for (detail in orderDetailsModel.details) {
      val maxProductNameLength = 27 // Adjust this based on your requirements
      val productNameSpacing = " ".repeat(maxProductNameLength - (detail.productDetails?.name?.length ?: 0))

      builder
        .styleAlignment(Alignment.Left)
        .add(
          PrinterBuilder()
            .styleBold(true)
            .actionPrintText("${detail.productDetails?.name}$productNameSpacing Qty:${detail.quantity}         $${detail.price?.formatToTwoDecimalPlaces()}\n")
        )
        .actionPrintText(
          "\n${getTextFromValue(detail.variations)}\n\n"
        )
    }

    builder
      .actionPrintText("----------------------------------------------\n")
      .styleAlignment(Alignment.Left)
      .actionPrintText("Order ID# ${orderDetailsModel.order.id}\n")
      .add(
        PrinterBuilder()
          .styleBold(true)
          .actionPrintText("Note:\n")
      )
      .actionPrintText(
        "----------------------------------------------\n" +
                "\n"
      )
      .styleAlignment(Alignment.Left)
      .actionPrintText(
        "Items Price                              $${calculateSubtotal(orderDetailsModel.details)}\n" +
                "Addons Price                             $0.00\n" +
                "Discount                                 $${
                  calculateTotalDiscount(
                    orderDetailsModel.details
                  )
                }\n" +
                "Tax                                      $${orderDetailsModel.order.tax?.formatToTwoDecimalPlaces()}\n" +
                "----------------------------------------------\n"
      )
      .add(
        PrinterBuilder()
          .styleMagnification(MagnificationParameter(2, 2))
          .actionPrintText("Total        $${calculateGrandTotal(orderDetailsModel)}\n")
      )
      .styleAlignment(Alignment.Left)
      .actionPrintText("\n---------------------------------------------\n")
      .styleAlignment(Alignment.Center)
      .actionFeedLine(1)
      .actionPrintQRCode(
        QRCodeParameter("Powered by HartApps.com\n")
          .setLevel(QRCodeLevel.L)
          .setCellSize(8)
      )
      .actionPrintText("\n\nPowered by HartApps.com\n\n")
      .actionCut(CutType.Partial)

    return builder
  }

  private fun getTextFromValue(oldVariationList: List<OldVariation>?): String {
    val result = StringBuilder()
    if (oldVariationList != null) {
      for (oldVariation in oldVariationList) {
        result.append("- ${oldVariation.name} ( ${oldVariation.value?.firstOrNull()?.label} )\n")
      }
    }
    return result.toString()
  }
}
