package com.hermesandroid.bridge

import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Zee Offline manual toggle — fires the Lighter Zee ritual via RUN_COMMAND
 * and sets/clears the manual marker so the auto-watchdog backs off while
 * the user is driving through dead zones (P9: the "one tap before you
 * leave" flow).
 */
object OfflineToggler {

    private const val TAG = "OfflineToggler"
    private const val PYTHON = "/data/data/com.termux/files/home/hermes-agent/venv/bin/python3"
    private const val RITUAL = "/data/data/com.termux/files/home/.hermes/scripts/zee_offline_ritual.py"
    private const val MANUAL_MARKER = "/data/data/com.termux/files/home/.hermes/state/zee_offline_manual.txt"

    /** mode: "on" or "off" */
    fun fire(ctx: Context, mode: String) {
        val cmd = if (mode == "on") {
            "$PYTHON $RITUAL on && touch $MANUAL_MARKER"
        } else {
            "$PYTHON $RITUAL off && rm -f $MANUAL_MARKER"
        }
        try {
            val intent = Intent("com.termux.RUN_COMMAND").apply {
                setClassName("com.termux", "com.termux.app.RunCommandService")
                putExtra("com.termux.RUN_COMMAND_PATH", "/data/data/com.termux/files/usr/bin/bash")
                putExtra("com.termux.RUN_COMMAND_ARGUMENTS", arrayOf("-c", cmd))
                putExtra("com.termux.RUN_COMMAND_BACKGROUND", true)
                putExtra("com.termux.RUN_COMMAND_SESSION_ACTION", 0)
            }
            ctx.startService(intent)
            Log.i(TAG, "Zee Offline $mode: RUN_COMMAND fired")
        } catch (e: SecurityException) {
            Log.e(TAG, "Zee Offline $mode BLOCKED: RUN_COMMAND permission missing", e)
        } catch (e: Exception) {
            Log.e(TAG, "Zee Offline $mode intent failed", e)
        }
    }
}
