package com.jotadev.aiapaec.data.storage

import android.content.Context
import android.content.SharedPreferences

object UserStorage {
    private const val PREF_NAME = "aiapaec_user_prefs"
    private const val KEY_NAME = "user_name"
    private const val KEY_EMAIL = "user_email"
    private const val KEY_INSTITUTION = "user_institution"
    private const val KEY_ROLE = "user_role"
    private const val KEY_BRANCH_ID = "user_branch_id"

    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        }
    }

    fun save(
        name: String?,
        email: String?,
        institution: String?,
        role: String?,
        branchId: Int?
    ) {
        prefs?.edit()?.apply {
            putString(KEY_NAME, name ?: "")
            putString(KEY_EMAIL, email ?: "")
            putString(KEY_INSTITUTION, institution ?: "")
            putString(KEY_ROLE, role ?: "")
            putInt(KEY_BRANCH_ID, branchId ?: 0)
            apply()
        }
    }

    fun getName(): String? = prefs?.getString(KEY_NAME, null)
    fun getEmail(): String? = prefs?.getString(KEY_EMAIL, null)
    fun getInstitution(): String? = prefs?.getString(KEY_INSTITUTION, null)
    fun getRole(): String? = prefs?.getString(KEY_ROLE, null)
    fun getBranchId(): Int? = prefs?.getInt(KEY_BRANCH_ID, 0)

    fun clear() {
        prefs?.edit()?.clear()?.apply()
    }
}