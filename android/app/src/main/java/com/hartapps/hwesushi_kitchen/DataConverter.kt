package com.hartapps.hwesushi_kitchen

class DataConverter {
    fun convertToOrderDetailsModel(data: Map<String, Any>): OrderDetailsModel {
        val orderMap = data["order"] as Map<String, Any>?
        val detailsList = data["details"] as List<Map<String, Any>>?

        val order = Order(
            id = orderMap?.get("id") as Int?,
            tableId = orderMap?.get("table_id") as Int?,
            numberOfPeople = orderMap?.get("number_of_people") as Int?,
            tableOrderId = orderMap?.get("table_order_id") as Int?,
            orderNote = orderMap?.get("order_note") as String?,
            orderStatus = orderMap?.get("order_status") as String?,
            couponDiscountAmount = convertToDouble(orderMap?.get("coupon_discount_amount")),
            tax = convertToDouble(orderMap?.get("total_tax_amount")),
            deliveryCharge = convertToDouble(orderMap?.get("delivery_charge")),
            extraDiscount = convertToDouble(orderMap?.get("extra_discount")),
            createdAt = orderMap?.get("created_at") as String?,
            deliveryDate = orderMap?.get("delivery_date") as String?,
            deliveryTime = orderMap?.get("delivery_time") as String?,
            customerName = orderMap?.get("customer_name") as String?,
            customerPhone = orderMap?.get("customer_phone") as String?,
            cardType = orderMap?.get("card_type") as String?,
            cardNumber = orderMap?.get("card_number") as String?,
            paymentMethod = orderMap?.get("payment_method") as String?,
            transactionReference = orderMap?.get("transaction_reference") as String?,
                    serviceFee =orderMap?.get("service_fee") as String?
            // Add other properties of the Order class
        )

        val details = detailsList?.map { detailMap ->
            Details(
                id = detailMap["id"] as Int?,
                productId = detailMap["product_id"] as Int?,
                orderId = detailMap["order_id"] as Int?,
                price = convertToDouble(detailMap["price"]),
                productDetails = convertToProductDetails(detailMap["product_details"] as Map<String, Any>?),
                variations = (detailMap["variation"] as List<Map<String, Any>>?)?.map { convertToOldVariation(it) },
                oldVariations = (detailMap["old_variations"] as List<Map<String, Any>>?)?.map { convertToOldVariation(it) },
                discountOnProduct = convertToDouble(detailMap["discount_on_product"]),
                discountType = detailMap["discount_type"] as String?,
                quantity = detailMap["quantity"] as Int?,
                taxAmount = detailMap["tax_amount"] as Double?,
                createdAt = detailMap["created_at"] as String?,
                updatedAt = detailMap["updated_at"] as String?,
                addOnIds = detailMap["add_on_ids"] as List<Int>?,
                addOnQtys = detailMap["add_on_qtys"] as List<Int>?,
                addOnPrices = detailMap["add_on_prices"] as List<Double>?,
                addonTaxAmount = convertToDouble(detailMap["add_on_tax_amount"]),
                globalTax = detailMap["global_tax"] as Double?
                // Add other properties of the Details class
            )
        } ?: emptyList()

        return OrderDetailsModel(order = order, details = details)
    }
    fun convertToDouble(value: Any?): Double? {
        return when (value) {
            is Double -> value
            is Int -> value.toFloat().toDouble()
            is Number -> value.toFloat().toDouble()
            is String -> value.toFloat().toDouble()
            else -> null
        }
    }
    fun convertToProductDetails(data: Map<String, Any>?): ProductDetails? {
        return data?.let {
            ProductDetails(
                id = it["id"] as Int?,
                name = it["name"] as String?,
                description = it["description"] as String?,
                image = it["image"] as String?,
                price = it["price"] as Double?,
                variations = (it["variations"] as List<Map<String, Any>>?)?.map { convertToOldVariation(it) },
                addOns = (it["add_ons"] as List<Map<String, Any>>?)?.map { convertToAddOns(it) },
                tax = it["tax"] as Double?,
                availableTimeStarts = it["available_time_starts"] as String?,
                availableTimeEnds = it["available_time_ends"] as String?,
                status = it["status"] as Int?,
                createdAt = it["created_at"] as String?,
                updatedAt = it["updated_at"] as String?,
                attributes = (it["attributes"] as List<String>?) ?: emptyList(),
                categoryIds = (it["category_ids"] as List<Map<String, Any>>?)?.map { convertToCategoryIds(it) },
                choiceOptions = (it["choice_options"] as List<Map<String, Any>>?)?.map { convertToChoiceOptions(it) },
                discount = convertToDouble(it["discount"]),
                discountType = it["discount_type"] as String?,
                taxType = it["tax_type"] as String?,
                setMenu = it["set_menu"] as Int?,
                popularityCount = it["popularity_count"] as Int?,
                productType = it["product_type"] as String?,
                // Add other properties of the ProductDetails class
            )
        }
    }

//    fun convertToVariations(data: Map<String, Any>?): Variations {
//        return Variations(
//            type = data?.get("type") as String?,
//            price = data?.get("price") as Double?
//        )
//    }

    fun convertToOldVariation(data: Map<String, Any>?): OldVariation {
        return OldVariation(
            type = data?.get("type") as String?,
            price = data?.get("price") as Double?,
            name = data?.get("name") as? String,
            value = (data?.get("values") as? List<Map<String, Any>>?)?.map { convertToValues(it) }
        )
    }

    private fun convertToValues(data: Map<String, Any>?): ValueClass {
        return ValueClass(
            label = data?.get("label") as? String,
            optionPrice = convertToDouble(data?.get("optionPrice"))
        )
    }

    fun convertToAddOns(data: Map<String, Any>?): AddOns {
        return AddOns(
            id = data?.get("id") as Int?,
            name = data?.get("name") as String?,
            price = data?.get("price") as Double?,
            createdAt = data?.get("created_at") as String?,
            updatedAt = data?.get("updated_at") as String?,
            translations = data?.get("translations") as List<Any>?
            // Add other properties of the AddOns class
        )
    }

    fun convertToCategoryIds(data: Map<String, Any>?): CategoryIds {
        return CategoryIds(
            id = data?.get("id") as String?,
            position = data?.get("position") as Int?
            // Add other properties of the CategoryIds class
        )
    }

    fun convertToChoiceOptions(data: Map<String, Any>?): ChoiceOptions {
        return ChoiceOptions(
            name = data?.get("name") as String?,
            title = data?.get("title") as String?,
            options = data?.get("options") as List<String>?
            // Add other properties of the ChoiceOptions class
        )
    }


}