import 'package:efood_kitchen/controller/order_controller.dart';
import 'package:efood_kitchen/data/model/response/order_details_model.dart';
import 'package:efood_kitchen/helper/price_converter.dart';
import 'package:efood_kitchen/helper/responsive_helper.dart';
import 'package:efood_kitchen/util/dimensions.dart';
import 'package:efood_kitchen/util/styles.dart';
import 'package:efood_kitchen/view/base/custom_divider.dart';
import 'package:flutter/material.dart';
import 'package:get/get.dart';

class CalculateAmountWidget extends StatefulWidget {
  final OrderController orderController;

  const CalculateAmountWidget({Key? key, required this.orderController})
      : super(key: key);

  @override
  State<CalculateAmountWidget> createState() => _CalculateAmountWidgetState();
}

class _CalculateAmountWidgetState extends State<CalculateAmountWidget> {
  double deliveryCharge = 0,
      couponDiscount = 0,
      extraDiscount = 0,
      serviceFee = 0;
  double itemsPrice = 0;
  double discount = 0;
  double tax = 0;
  double addOns = 0;
  double total = 0;

  @override
  void initState() {
    deliveryCharge =
        widget.orderController.orderDetails.order.deliveryCharge?.toDouble() ??
            0;
    couponDiscount = widget
            .orderController.orderDetails.order.couponDiscountAmount
            ?.toDouble() ??
        0;
    extraDiscount =
        widget.orderController.orderDetails.order.extraDiscount?.toDouble() ??
            0;
    print(
        "serviceFee11====${widget.orderController.orderDetails.order.serviceFee}");
    serviceFee =
        double.parse(widget.orderController.orderDetails.order.serviceFee==null?"0":widget.orderController.orderDetails.order.serviceFee.toString()) ?? 0;

    for (Details orderDetails in widget.orderController.orderDetails.details) {
      itemsPrice = itemsPrice + (orderDetails.price! * orderDetails.quantity!);
      discount =
          discount + (orderDetails.discountOnProduct! * orderDetails.quantity!);
      tax = orderDetails.globalTax ?? 0;

      List<int> ids = orderDetails.addOnIds ?? [];

      if (ids.length == orderDetails.addOnPrices?.length &&
          ids.length == orderDetails.addOnQtys?.length) {
        for (int i = 0; i < ids.length; i++) {
          addOns = addOns +
              (orderDetails.addOnPrices![i] * orderDetails.addOnQtys![i]);
        }
      }
    }

    double subTotal =
        itemsPrice + addOns - (discount + couponDiscount + extraDiscount);
    tax = (subTotal * tax) / 100;

    // TODO: implement initState
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        CalculateItem(title: 'items_price', amount: itemsPrice),
        CalculateItem(title: 'addons_price', amount: addOns),
        CalculateItem(title: 'discount', amount: discount, isDiscount: true),
        if (extraDiscount > 0)
          CalculateItem(
              title: 'extra_discount'.tr,
              amount: extraDiscount,
              isDiscount: true),
        CalculateItem(title: 'vat_tax', amount: tax),
        if (couponDiscount > 0)
          CalculateItem(
              title: 'coupon_discount',
              amount: couponDiscount,
              isDiscount: true),
        if (deliveryCharge > 0)
          CalculateItem(title: 'delivery_charge', amount: deliveryCharge),
        const CustomDivider(),
        const SizedBox(height: Dimensions.paddingSizeSmall),
        CalculateItem(title: 'Subtotal', amount: total, isTotal: true),
        CalculateItem(title: 'Service Fee', amount: serviceFee),
        const CustomDivider(),
        CalculateItem(title: 'Total', amount: total+ serviceFee, isTotal: true),
      ],
    );
  }
}

class CalculateItem extends StatelessWidget {
  final String title;
  final double amount;
  final bool isTotal;
  final bool isDiscount;

  const CalculateItem({
    Key? key,
    required this.title,
    required this.amount,
    this.isTotal = false,
    this.isDiscount = false,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final bool isSmall = ResponsiveHelper.isSmallTab();
    return Padding(
      padding: EdgeInsets.only(
        bottom: isSmall
            ? Dimensions.paddingSizeExtraSmall
            : Dimensions.paddingSizeDefault,
      ),
      child: Row(mainAxisAlignment: MainAxisAlignment.spaceBetween, children: [
        isTotal
            ? Text(title.tr,
                style: robotoBold.copyWith(
                    fontSize: Dimensions.fontSizeExtraLarge))
            : Text(title.tr,
                style:
                    robotoRegular.copyWith(fontSize: Dimensions.fontSizeLarge)),
        isTotal
            ? Text(
                PriceConverter.convertPrice(context, amount),
                style: robotoBold.copyWith(fontSize: Dimensions.fontSizeLarge),
              )
            : Text(
                '${isDiscount ? '(-) ' : ''}${PriceConverter.convertPrice(context, amount)}',
                style:
                    robotoRegular.copyWith(fontSize: Dimensions.fontSizeLarge),
              ),
      ]),
    );
  }
}
