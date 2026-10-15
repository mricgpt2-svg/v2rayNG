package com.v2ray.ang.ui.main

import android.content.Context
import com.v2ray.ang.dto.entities.SubscriptionItem
import com.v2ray.ang.handler.MmkvManager
import com.v2ray.ang.handler.SettingsChangeManager
import com.v2ray.ang.handler.SubscriptionUpdater
import java.util.UUID

object MyVpnSubscriptionHelper {

    fun addSubscription(context: Context, url: String): Boolean {
        val link = url.trim()

        if (!link.startsWith("https://", ignoreCase = true) &&
            !link.startsWith("http://", ignoreCase = true)) {
            return false
        }

        val existing = MmkvManager.decodeSubscriptions()
            .firstOrNull { it.subscription.url == link }

        val subId = existing?.guid ?: UUID.randomUUID().toString()

        if (existing == null) {
            val item = SubscriptionItem().apply {
                remarks = "MyVPN"
                this.url = link
                enabled = true
            }

            MmkvManager.encodeSubscription(subId, item)
        }

        SubscriptionUpdater.syncOne(context, subId)
        SettingsChangeManager.makeSetupGroupTab()

        return true
    }
}
