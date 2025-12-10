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
    private const val KEY_BRANCH_NAME = "user_branch_name"
    private const val KEY_REMEMBER_EMAIL = "remember_email" // Email para login recordado
    private const val KEY_REMEMBER_FLAG = "remember_flag" // Estado del checkbox Recordar

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
        branchId: Int?,
        branchName: String?
    ) {
        prefs?.edit()?.apply {
            putString(KEY_NAME, name ?: "")
            putString(KEY_EMAIL, email ?: "")
            putString(KEY_INSTITUTION, institution ?: "")
            putString(KEY_ROLE, role ?: "")
            putInt(KEY_BRANCH_ID, branchId ?: 0)
            putString(KEY_BRANCH_NAME, branchName ?: "")
            apply()
        }
    }

    fun getName(): String? = prefs?.getString(KEY_NAME, null)
    fun getEmail(): String? = prefs?.getString(KEY_EMAIL, null)
    fun getInstitution(): String? = prefs?.getString(KEY_INSTITUTION, null)
    fun getRole(): String? = prefs?.getString(KEY_ROLE, null)
    fun getBranchId(): Int? = prefs?.getInt(KEY_BRANCH_ID, 0)
    fun getBranchName(): String? = prefs?.getString(KEY_BRANCH_NAME, null)

    fun clear() {
        prefs?.edit()?.clear()?.apply()
    }

    // Limpia solo el email guardado
    fun clearEmail() {
        prefs?.edit()?.putString(KEY_EMAIL, "")?.apply()
    }

    // Guarda el email para precarga de login si el usuario marcó "Recordar"
    fun saveRememberedEmail(email: String?) {
        prefs?.edit()?.putString(KEY_REMEMBER_EMAIL, email ?: "")?.apply()
    }

    // Obtiene el email recordado para el login
    fun getRememberedEmail(): String? = prefs?.getString(KEY_REMEMBER_EMAIL, null)

    // Limpia el email recordado para login
    fun clearRememberedEmail() {
        prefs?.edit()?.putString(KEY_REMEMBER_EMAIL, "")?.apply()
    }

    // Guarda el estado del checkbox Recordar
    fun saveRememberFlag(remember: Boolean) {
        prefs?.edit()?.putBoolean(KEY_REMEMBER_FLAG, remember)?.apply()
    }

    // Obtiene el estado del checkbox Recordar
    fun getRememberFlag(): Boolean = prefs?.getBoolean(KEY_REMEMBER_FLAG, false) ?: false

    // Limpia el estado del checkbox Recordar
    fun clearRememberFlag() {
        prefs?.edit()?.putBoolean(KEY_REMEMBER_FLAG, false)?.apply()
    }
}
