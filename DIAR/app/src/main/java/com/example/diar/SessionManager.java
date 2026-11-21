
package com.example.diar;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "DIAR_SESSION";

    private static final String KEY_EMAIL = "email";
    private static final String KEY_NOM = "nom";
    private static final String KEY_TYPE = "typeCompte";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(
                PREF_NAME,
                Context.MODE_PRIVATE
        );
    }

    public void sauvegarderSession(
            String email,
            String nom,
            String typeCompte
    ) {
        prefs.edit()
                .putString(KEY_EMAIL, normaliserEmail(email))
                .putString(KEY_NOM, nom)
                .putString(KEY_TYPE, typeCompte)
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .apply();
    }

    public void sauvegarderSession(String email, String nom) {
        sauvegarderSession(email, nom, "ETUDIANT");
    }

    public void saveSession(
            String email,
            String nom,
            String typeCompte
    ) {
        sauvegarderSession(email, nom, typeCompte);
    }

    public void saveSession(String email, String nom) {
        sauvegarderSession(email, nom, "ETUDIANT");
    }

    public void saveEmail(String email) {
        prefs.edit()
                .putString(KEY_EMAIL, normaliserEmail(email))
                .apply();
    }

    public void saveNom(String nom) {
        prefs.edit().putString(KEY_NOM, nom).apply();
    }

    public void saveType(String type) {
        prefs.edit().putString(KEY_TYPE, type).apply();
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, null);
    }

    public String getNom() {
        return prefs.getString(KEY_NOM, null);
    }

    public String getTypeCompte() {
        return prefs.getString(KEY_TYPE, null);
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void logout() {
        prefs.edit().clear().apply();
    }

    public void clearSession() {
        logout();
    }

    public static String normaliserEmail(String email) {
        if (email == null) {
            return null;
        }
        return email.trim().toLowerCase();
    }
}
