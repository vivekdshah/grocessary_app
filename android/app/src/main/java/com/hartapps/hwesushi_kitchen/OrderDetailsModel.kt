package com.hartapps.hwesushi_kitchen

data class OrderDetailsModel(
    val order: Order,
    val details: List<Details>
)

data class Order(
    val id: Int?,
    val tableId: Int?,
    val numberOfPeople: Int?,
    val tableOrderId: Int?,
    val orderNote: String?,
    val orderStatus: String?,
    val couponDiscountAmount: Double?,
    val tax: Double?,
    val deliveryCharge: Double?,
    val serviceFee: String?,
    val extraDiscount: Double?,
    val createdAt: String?,
    val deliveryDate: String?,
    val deliveryTime: String?,
    val customerName: String?,
    val customerPhone: String?,
    val cardType: String?,
    val cardNumber: String?,
    val paymentMethod: String?,
    val transactionReference: String?
)

data class Details(
    val id: Int?,
    val productId: Int?,
    val orderId: Int?,
    val price: Double?,
    val productDetails: ProductDetails?,
    val variations: List<OldVariation>?,
    val oldVariations: List<OldVariation>?,
    val discountOnProduct: Double?,
    val discountType: String?,
    val quantity: Int?,
    val taxAmount: Double?,
    val createdAt: String?,
    val updatedAt: String?,
    val addOnIds: List<Int>?,
    val addOnQtys: List<Int>?,
    val addOnPrices: List<Double>?,
    val addonTaxAmount: Double?,
    val globalTax: Double?
)

// Define other necessary data classes like ProductDetails, Variations, OldVariation, etc.
data class ProductDetails(
    val id: Int?,
    val name: String?,
    val description: String?,
    val image: String?,
    val price: Double?,
    val variations: List<OldVariation>?,
    val addOns: List<AddOns>?,
    val tax: Double?,
    val availableTimeStarts: String?,
    val availableTimeEnds: String?,
    val status: Int?,
    val createdAt: String?,
    val updatedAt: String?,
    val attributes: List<String>?,
    val categoryIds: List<CategoryIds>?,
    val choiceOptions: List<ChoiceOptions>?,
    val discount: Double?,
    val discountType: String?,
    val taxType: String?,
    val setMenu: Int?,
    val popularityCount: Int?,
    val productType: String?
)

//data class Variations(
//    val type: String?,
//    val price: Double?
//)

data class AddOns(
    val id: Int?,
    val name: String?,
    val price: Double?,
    val createdAt: String?,
    val updatedAt: String?,
    val translations: List<Any>? // Adjust the type accordingly
)

data class CategoryIds(
    val id: String?,
    val position: Int?
)

data class ChoiceOptions(
    val name: String?,
    val title: String?,
    val options: List<String>?
)

// Define other necessary data classes as needed.

// For simplicity, you can use the same structure for OldVariation as Variation.
data class OldVariation(
    val type: String?,
    val price: Double?,
    val name: String?,
    val value: List<ValueClass>?
)

data class ValueClass(
    val label: String?,
    val optionPrice: Double?
)
