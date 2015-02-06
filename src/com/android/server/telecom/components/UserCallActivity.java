/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.server.telecom.components;

import com.android.server.telecom.CallIntentProcessor;
import com.android.server.telecom.Log;
import com.android.server.telecom.UserCallIntentProcessor;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.telecom.TelecomManager;

// TODO: Needed for move to system service: import com.android.internal.R;

/**
 * Activity that handles system CALL actions and forwards them to {@link CallIntentProcessor}.
 * Handles all three CALL action types: CALL, CALL_PRIVILEGED, and CALL_EMERGENCY.
 *
 * Pre-L, the only way apps were were allowed to make outgoing emergency calls was the
 * ACTION_CALL_PRIVILEGED action (which requires the system only CALL_PRIVILEGED permission).
 *
 * In L, any app that has the CALL_PRIVILEGED permission can continue to make outgoing emergency
 * calls via ACTION_CALL_PRIVILEGED.
 *
 * In addition, the default dialer (identified via
 * {@link TelecomManager#getDefaultPhoneApp()} will also be granted the ability to
 * make emergency outgoing calls using the CALL action. In order to do this, it must call
 * startActivityForResult on the CALL intent to allow its package name to be passed to
 * {@link UserCallActivity}. Calling startActivity will continue to work on all non-emergency numbers
 * just like it did pre-L.
 */
public class UserCallActivity extends Activity {

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        // TODO: Figure out if there is something to restore from bundle.
        // See OutgoingCallBroadcaster in services/Telephony for more.
        Intent intent = getIntent();
        verifyCallAction(intent);
        new UserCallIntentProcessor(this).processIntent(getIntent(), getCallingPackage());
        finish();
    }

    private void verifyCallAction(Intent intent) {
        if (getClass().getName().equals(intent.getComponent().getClassName())) {
            // If we were launched directly from the CallActivity, not one of its more privileged
            // aliases, then make sure that only the non-privileged actions are allowed.
            if (!Intent.ACTION_CALL.equals(intent.getAction())) {
                Log.w(this, "Attempt to deliver non-CALL action; forcing to CALL");
                intent.setAction(Intent.ACTION_CALL);
            }
        }
    }
}
