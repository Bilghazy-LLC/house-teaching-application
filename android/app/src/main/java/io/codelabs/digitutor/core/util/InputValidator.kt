@file:JvmName("InputValidator")

package io.codelabs.digitutor.core.util

import android.util.Patterns

object InputValidator {

    /**
     * Validate email fields
     */
    fun isValidEmail(email: String): Boolean = email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()

    /**
     * Validate all other input fields
     */
    fun hasValidInput(vararg inputs: String): Boolean {
        var valid = false
        for (input in inputs) {
            valid = input.isNotEmpty()
        }
        return valid
    }
}