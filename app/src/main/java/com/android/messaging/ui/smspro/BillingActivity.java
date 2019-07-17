package com.android.messaging.ui.smspro;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.ad.BillingManager;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.BugleFirebaseAnalytics;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

import net.appcloudbox.internal.service.iap.ProductRequest;
import net.appcloudbox.service.iap.AcbIAPProduct;
import net.appcloudbox.service.iap.AcbIAPProductRequest;
import net.appcloudbox.service.utils.AcbError;
import net.appcloudbox.service.utils.AcbResponseListener;

import java.util.ArrayList;

import static com.android.messaging.ad.BillingManager.BILLING_VERIFY_SUCCESS;
import static com.android.messaging.ad.BillingManager.PRODUCT_ID;

public class BillingActivity extends AppCompatActivity implements INotificationObserver{

    private AcbIAPProductRequest acbIAPProductRequest;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_billing);

        ArrayList<String> list = new ArrayList<>(2);
        list.add(PRODUCT_ID);

        TextView currencyCodeTv = findViewById(R.id.currency_code_text_view);
        TextView priceTv = findViewById(R.id.price_text_view);
        TextView purchaseButton = findViewById(R.id.purchase_text_view);

        acbIAPProductRequest = new AcbIAPProductRequest(list, false /*是否为订阅类型*/, new AcbResponseListener<ProductRequest.Result>() {
            @Override
            public void onSuccess(ProductRequest.Result result) {
                AcbIAPProduct product = result.getProducts().get(0);
                String price = product.getPrice();
                int amountStartIndex = 0;

                for (int i = 0, length = price.length(); i < length; i++) {
                    if (Character.isDigit(price.charAt(i))) {
                        amountStartIndex = i;
                        break;
                    }
                }
                currencyCodeTv.setText(price.substring(0, amountStartIndex));
                priceTv.setText(price.substring(amountStartIndex));
            }

            @Override
            public void onFailure(AcbError error) {
                // 获取失败，原因详见error.getKey()
            }
        });
        acbIAPProductRequest.start();
        purchaseButton.setBackground(BackgroundDrawables.
                createBackgroundDrawable(0xff1db255, Dimensions.pxFromDp(27), true));
        purchaseButton.setOnClickListener(v -> {
            BillingManager.requestPurchase();

            BugleAnalytics.logEvent("SMS_Subscription_Click");
            BugleAnalytics.logEvent("Subscription_Analysis", "Subscription_Click", "true");
            BugleFirebaseAnalytics.logEvent("Subscription_Analysis", "Subscription_Click", "true");
        });

        ImageView closeActionImage = findViewById(R.id.action_close);
        closeActionImage.setOnClickListener(v -> finish());

        BugleAnalytics.logEvent("SMS_Subscription_Show", true);
        BugleAnalytics.logEvent("Subscription_Analysis", "Subscription_Show", "true");
        BugleFirebaseAnalytics.logEvent("Subscription_Analysis", "Subscription_Show", "true");

        HSGlobalNotificationCenter.addObserver(BILLING_VERIFY_SUCCESS, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        acbIAPProductRequest.cancel();
        HSGlobalNotificationCenter.removeObserver(this);
    }

    @Override
    public void onReceive(String s, HSBundle hsBundle) {
        if (BILLING_VERIFY_SUCCESS.equals(s)) {
            finish();
        }
    }

}
