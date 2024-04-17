//package com.hartapps.hwesushi_kitchen
//
//import io.flutter.embedding.android.FlutterActivity
//
//class MainActivity: FlutterActivity() {
//}
package com.hartapps.hwesushi_kitchen

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.starmicronics.stario10.InterfaceType
import com.starmicronics.stario10.StarConnectionSettings
import com.starmicronics.stario10.StarPrinter
import com.starmicronics.stario10.starxpandcommand.DocumentBuilder
import com.starmicronics.stario10.starxpandcommand.MagnificationParameter
import com.starmicronics.stario10.starxpandcommand.PrinterBuilder
import com.starmicronics.stario10.starxpandcommand.StarXpandCommandBuilder
import com.starmicronics.stario10.starxpandcommand.printer.Alignment
import com.starmicronics.stario10.starxpandcommand.printer.CutType
import com.starmicronics.stario10.starxpandcommand.printer.QRCodeLevel
import com.starmicronics.stario10.starxpandcommand.printer.QRCodeParameter
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MainActivity : FlutterActivity() {
    private val CHANNEL = "samples.flutter.dev/MyChannel"
    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(
                flutterEngine.dartExecutor.binaryMessenger,
                CHANNEL
        ).setMethodCallHandler { call, result ->
            if (call.method == "open_printer_setting") {
                startActivity(Intent(this, DiscoveryActivity::class.java))
                result.success("")
            } else if (call.method == "print_receipt") {
                android.util.Log.i("Hello", "I am from project_native")
                // Get the data from the Flutter method call
                val data = call.argument<Map<String, Any>>("data")
                // Check if the data is not null
                if (data != null) {
                    // Convert the data to the appropriate Kotlin data class
                    val converterClass = DataConverter()
                    val orderDetailsModel = converterClass.convertToOrderDetailsModel(data)
                    android.util.Log.i("HelloDate", "${orderDetailsModel.order.deliveryDate?.formatDate()}  ${orderDetailsModel.order.deliveryTime?.formatTime()}")
                    // Now you can use the orderDetailsModel as needed
                    // For example, pass it to your print logic
                    getPrintBuilder(orderDetailsModel)
                    showMessage("Preparing printer")
                    onPressPrintButton(orderDetailsModel)
                } else {
                    showMessage("Receipt not found")
                }

                result.success("")
            }
        }
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
        val printer = StarPrinter(settings, applicationContext)

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

        return checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
    }

    private fun showMessage(message: String) {
        android.util.Log.i("PRINTER_MESSAGE_FROM_APP_ANDROID", message)
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun getPrinterIdentifier(): String {
        val prefs = getSharedPreferences("PrinterPrefs", MODE_PRIVATE)
        return prefs.getString("printerIdentifier", "") ?: ""
    }

    private fun getPrinterInterface(): String {
        val prefs = getSharedPreferences("PrinterPrefs", MODE_PRIVATE)
        return prefs.getString("printerInterfaceType", "") ?: ""
    }

    private fun calculateSubtotal(details: List<Details>): String {
        val subtotal = details.sumByDouble { (it.price ?: 0.0) * (it.quantity ?: 0) }
        return "%.2f".format(subtotal)
    }
    private fun getItemTotal(itemPrice: Double?, quantity: Int?): String {
        val total: Double = (itemPrice ?: 0.0) * (quantity ?: 0)
        return total.formatToTwoDecimalPlaces()
    }

    private fun calculateTotalDiscount(details: List<Details>): String {
        val totalDiscount = details.sumByDouble { it.discountOnProduct ?: 0.0 }
        return "%.2f".format(totalDiscount)
    }
    private fun getTotalTax(orderDetailsModel: OrderDetailsModel): Double {
        val subtotal = calculateSubtotal(orderDetailsModel.details).toDouble()
        val discount = calculateTotalDiscount(orderDetailsModel.details).toDouble()
        val couponDiscount = orderDetailsModel.order.couponDiscountAmount ?: 0.0
        val totalTaxableAmount = subtotal - discount - couponDiscount
        val tax = ((orderDetailsModel.details.first().globalTax ?: 0.0) * totalTaxableAmount) / 100
        return tax
    }

    private fun calculateGrandTotal(orderDetailsModel: OrderDetailsModel): String {
        val subtotal = calculateSubtotal(orderDetailsModel.details).toDouble()
        val discount = calculateTotalDiscount(orderDetailsModel.details).toDouble()
        val couponDiscount = orderDetailsModel.order.couponDiscountAmount ?: 0.0
        var serviceFee = 0.0;
        if(orderDetailsModel.order.serviceFee != null){
         serviceFee = orderDetailsModel.order.serviceFee.toDouble();
        }
        val totalTaxableAmount = subtotal - discount - couponDiscount + serviceFee
        val grandTotal = totalTaxableAmount + getTotalTax(orderDetailsModel)


        // Format to two decimal places
        return "%.2f".format(grandTotal)
    }

    private fun calculateGrandSubTotal(orderDetailsModel: OrderDetailsModel): String {
        val subtotal = calculateSubtotal(orderDetailsModel.details).toDouble()
        val discount = calculateTotalDiscount(orderDetailsModel.details).toDouble()
        val couponDiscount = orderDetailsModel.order.couponDiscountAmount ?: 0.0
        val totalTaxableAmount = subtotal - discount - couponDiscount
        val grandTotal = totalTaxableAmount + getTotalTax(orderDetailsModel)

        // Format to two decimal places
        return "%.2f".format(grandTotal)
    }

    private fun getPrintBuilder(orderDetailsModel: OrderDetailsModel): PrinterBuilder {
        val builder = PrinterBuilder()
                .styleAlignment(Alignment.Center)
                .add(
                        PrinterBuilder()
                                .styleMagnification(MagnificationParameter(2, 3))
                                .actionPrintText("Cresskill Hot Bagel\n")
                )
                .add(
                        PrinterBuilder()
                                .styleMagnification(MagnificationParameter(2, 2))
                                .actionPrintText("\n")
                )
                .styleAlignment(Alignment.Left)
                .actionPrintText(
                        "${orderDetailsModel.order.createdAt?.formatDateTime()}" +
                                "\nOrder ID# ${orderDetailsModel.order.id}\n" +
                                "\nName: ${orderDetailsModel.order.customerName}" +
                                "\nPhone: ${formatPhoneNumber(orderDetailsModel.order.customerPhone)}" +
                                "\nCC Last 4: ${ if (orderDetailsModel.order.cardNumber.isNullOrBlank()) "N/A" else orderDetailsModel.order.cardNumber}" +
                                "\nRef#: ${ if (orderDetailsModel.order.transactionReference.isNullOrBlank()) "N/A" else orderDetailsModel.order.transactionReference}" +
                                "\nPayment Method: ${orderDetailsModel.order.paymentMethod}" +
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
                                .actionPrintText("\nDate: ${orderDetailsModel.order.deliveryDate?.formatDate()}   Time: ${orderDetailsModel.order.deliveryTime?.formatTime()}")
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

        for ((index, detail) in orderDetailsModel.details.withIndex()) {
            var productName = detail.productDetails?.name
            val maxProductNameLength = 27 // Adjust this based on your requirements
            var remainingChars = ""
            if ((productName?.length ?: 0) > (maxProductNameLength - 1)) {
                if ((maxProductNameLength - 3) < (productName?.count() ?: 0)) {
                    remainingChars = productName?.drop(maxProductNameLength - 3) ?: ""
                    remainingChars.trim()
                }
                productName = "${productName?.take(maxProductNameLength - 3)}"
            }

            val productNameSpacing = " ".repeat(maxProductNameLength - (productName?.length ?: 0))

            builder
                    .styleAlignment(Alignment.Left)
                    .add(
                            PrinterBuilder()
                                    .styleBold(true)
                                    .actionPrintText("${productName}$productNameSpacing Qty:${detail.quantity}         $${getItemTotal(detail.price, detail.quantity)}\n")
                    )

            if (remainingChars.isNotBlank()) {
                builder
                        .add(
                                PrinterBuilder()
                                        .styleBold(true)
                                        .actionPrintText("$remainingChars\n")
                        )
            }

            builder
                    .actionPrintText("\n${getTextFromValue(detail.variations)}\n")
            if (index < orderDetailsModel.details.size - 1) {
                builder.actionPrintText("----------------------------------------------\n")
            } else {
                builder.actionPrintText("\n")
            }
        }

        builder
                .actionPrintText("----------------------------------------------\n")
                .styleAlignment(Alignment.Left)
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
                                "Tax                                      $${getTotalTax(orderDetailsModel).formatToTwoDecimalPlaces()}\n" +
                                "Coupon Discount                         -$${orderDetailsModel.order.couponDiscountAmount?.formatToTwoDecimalPlaces()}\n" +
                                "----------------------------------------------\n"
                )
                .add(
                        PrinterBuilder()
                                .styleMagnification(MagnificationParameter(2, 2))
                                .actionPrintText("Subtotal          $${calculateGrandSubTotal(orderDetailsModel)}\n")
                )
                .add(
                        PrinterBuilder()
                                .styleMagnification(MagnificationParameter(2, 2))
                                .actionPrintText("ServiceFee          -$${orderDetailsModel.order.serviceFee}\n")
                )
                .actionPrintText("\n---------------------------------------------\n")
                .add(
                        PrinterBuilder()
                                .styleMagnification(MagnificationParameter(2, 2))
                                .actionPrintText("Total          $${calculateGrandTotal(orderDetailsModel)}\n")
                )
                .styleAlignment(Alignment.Left)
                .actionPrintText("\n---------------------------------------------\n")
                .styleAlignment(Alignment.Center)
                .actionFeedLine(1)
                .actionPrintQRCode(
                        QRCodeParameter("https://hartapps.page.link/applinks\n")
                                .setLevel(QRCodeLevel.L)
                                .setCellSize(8)
                )
                .actionPrintText("\nPowered by HartApps.com\n\n")
                .actionCut(CutType.Partial)

        return builder
    }

    private fun getTextFromValue(oldVariationList: List<OldVariation>?): String {
        val result = StringBuilder()
        if (oldVariationList != null) {
            for (oldVariation in oldVariationList) {
                oldVariation.value?.let { itemList ->
                    for (item in itemList) {
                        result.append("- ${item.label}")
                        if ((item.optionPrice ?: 0.0) > 0) {
                            result.append(" $${item.optionPrice?.formatToTwoDecimalPlaces()}")
                        }
                        result.append("\n")
                    }
                }
            }
        }
        return result.toString()
    }

    fun formatPhoneNumber(phoneNumber: String?): String {
        var formattedPhoneNumber = phoneNumber?.replace("US", "") ?: ""
        formattedPhoneNumber = formattedPhoneNumber?.replace("+1", "") ?: ""

        if (formattedPhoneNumber.isBlank()) {
            return "" // Return empty string for null or numbers with length less than 10
        }

        val countryCode = formattedPhoneNumber.substring(0, 3)
        val areaCode = formattedPhoneNumber.substring(3, 6)
        val remainingDigits = formattedPhoneNumber.substring(6)

        return if (remainingDigits.length > 4) {
            val firstDigits = remainingDigits.substring(0, 3)
            val lastDigits = remainingDigits.substring(3)
            "($countryCode) $areaCode-$firstDigits-$lastDigits"
        } else {
            "($countryCode) $areaCode-$remainingDigits"
        }
    }

}
