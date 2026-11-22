package com.example.diar;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Interface21 extends AppCompatActivity {

    private LinearLayout layoutConversations;
    private EditText edtRecherche;

    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;

    private boolean proprietaire;
    private String emailActuel;
    private String nomActuel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interface21);

        databaseHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        String type = getIntent().getStringExtra("typeCompte");

        if (type == null) {
            type = getIntent().getStringExtra("typeUtilisateur");
        }

        proprietaire =
                "PROPRIETAIRE".equalsIgnoreCase(type)
                        || "proprietaire".equalsIgnoreCase(type)
                        || getIntent().getStringExtra("emailProprietaire") != null;

        if (proprietaire) {

            emailActuel = getIntent().getStringExtra("emailProprietaire");

            if (emailActuel == null || emailActuel.trim().isEmpty()) {
                emailActuel = sessionManager.getEmail();
            }

            nomActuel = getIntent().getStringExtra("nomProprietaire");

            NavigationProprietaire.setup(
                    this,
                    R.id.nav_prop_messages
            );

        } else {

            emailActuel = sessionManager.getEmail();

            nomActuel = getIntent().getStringExtra("nomEtudiant");

            NavigationEtudiant.setup(this, 3);
        }

        layoutConversations = findViewById(R.id.layoutConversations);
        edtRecherche = findViewById(R.id.edtRechercheConversation);

        if (emailActuel == null || emailActuel.trim().isEmpty()) {
            Toast.makeText(
                    this,
                    "Email introuvable",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        chargerConversationsSQLite();
        configurerRecherche();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Au retour d'un chat, les messages lus / nouveaux sont mis à jour
        if (databaseHelper != null
                && layoutConversations != null
                && emailActuel != null
                && !emailActuel.trim().isEmpty()) {

            chargerConversationsSQLite();
        }
    }

    private int dp(float valeur) {
        return (int) (valeur * getResources()
                .getDisplayMetrics().density);
    }

    // CHARGER LES CONVERSATIONS DEPUIS SQLITE

    private void chargerConversationsSQLite() {

        if (layoutConversations == null) {
            Toast.makeText(
                    this,
                    "layoutConversations introuvable",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        layoutConversations.removeAllViews();

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        String selection;

        if (proprietaire) {
            selection = "emailProprietaire = ?";
        } else {
            selection = "emailEtudiant = ?";
        }

        Cursor cursor = null;
        boolean trouve = false;

        try {

            cursor = db.query(
                    "Conversation",
                    new String[]{
                            "id",
                            "emailEtudiant",
                            "emailProprietaire",
                            "nomEtudiant",
                            "nomProprietaire"
                    },
                    selection,
                    new String[]{emailActuel},
                    null,
                    null,
                    "id DESC"
            );

            while (cursor.moveToNext()) {

                trouve = true;

                int id = cursor.getInt(0);

                String emailEtudiant = cursor.getString(1);
                String emailProprietaire = cursor.getString(2);

                String nomEtudiant = cursor.getString(3);
                String nomProprietaire = cursor.getString(4);

                String nomContact;
                String emailContact;

                if (proprietaire) {
                    nomContact = nomEtudiant;
                    emailContact = emailEtudiant;
                } else {
                    nomContact = nomProprietaire;
                    emailContact = emailProprietaire;
                }

                ajouterConversation(
                        id,
                        nomContact,
                        emailContact
                );
            }

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Erreur SQLite : " + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();

        } finally {

            if (cursor != null) {
                cursor.close();
            }
        }

        if (!trouve) {

            TextView vide = new TextView(this);

            vide.setText("Aucune conversation pour le moment");
            vide.setTextSize(16);
            vide.setTextColor(0xFF64748B);
            vide.setPadding(30, 40, 30, 40);

            layoutConversations.addView(vide);
        }

        appliquerRecherche();
    }

    // AJOUTER UNE CARTE CONVERSATION

    private void ajouterConversation(
            int conversationId,
            String nom,
            String emailContact) {

        // Nombre de messages non lus dans cette conversation
        int nonLus = databaseHelper.compterMessagesNonLus(
                conversationId,
                emailActuel
        );

        LinearLayout carte = new LinearLayout(this);

        carte.setOrientation(LinearLayout.HORIZONTAL);
        carte.setGravity(android.view.Gravity.CENTER_VERTICAL);
        carte.setPadding(30, 25, 30, 25);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        params.setMargins(10, 8, 10, 8);
        carte.setLayoutParams(params);

        if (nonLus > 0) {
            // Carte colorée en rouge clair (messages non lus)
            UnreadBadge.colorerCarte(carte, nonLus, 20);
        } else {
            carte.setBackgroundResource(R.drawable.bg_input_message);
        }

        carte.setElevation(3f);
        carte.setClickable(true);
        carte.setFocusable(true);

        String nomFinal =
                nom == null || nom.trim().isEmpty()
                        ? "Utilisateur"
                        : nom;

        TextView initiale = new TextView(this);

        initiale.setText(
                nomFinal.substring(0, 1).toUpperCase()
        );

        initiale.setTextSize(22);
        initiale.setTextColor(0xFF1B8A5A);
        initiale.setGravity(android.view.Gravity.CENTER);
        initiale.setBackgroundResource(
                R.drawable.bg_circle_green_light
        );

        LinearLayout.LayoutParams avatarParams =
                new LinearLayout.LayoutParams(54, 54);

        initiale.setLayoutParams(avatarParams);

        LinearLayout informations = new LinearLayout(this);
        informations.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams infoParams =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                );

        infoParams.setMargins(25, 0, 0, 0);
        informations.setLayoutParams(infoParams);

        TextView tvNom = new TextView(this);

        tvNom.setText(nomFinal);
        tvNom.setTextSize(16);
        tvNom.setTextColor(0xFF1E2A5A);
        tvNom.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        TextView tvEmail = new TextView(this);

        tvEmail.setText(
                emailContact == null ? "" : emailContact
        );

        tvEmail.setTextSize(13);
        tvEmail.setTextColor(0xFF64748B);

        informations.addView(tvNom);
        informations.addView(tvEmail);

        carte.addView(initiale);
        carte.addView(informations);

        // Cercle rouge avec le nombre de messages non lus
        if (nonLus > 0) {

            TextView badge = UnreadBadge.creer(this, nonLus);

            LinearLayout.LayoutParams badgeParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            badgeParams.setMargins(dp(8), 0, 0, 0);

            carte.addView(badge, badgeParams);
        }

        carte.setOnClickListener(v ->
                ouvrirConversation(
                        conversationId,
                        nomFinal,
                        emailContact
                )
        );

        layoutConversations.addView(carte);
    }

    // OUVRIR LE CHAT

    private void ouvrirConversation(
            int conversationId,
            String nom,
            String emailContact) {

        if (emailContact == null || emailContact.trim().isEmpty()) {

            Toast.makeText(
                    this,
                    "Email du contact introuvable",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        Intent intent;

        if (proprietaire) {

            intent = new Intent(
                    Interface21.this,
                    ChatProprietaire.class
            );

            intent.putExtra("emailProprietaire", emailActuel);
            intent.putExtra("proprietaireEmail", emailActuel);
            intent.putExtra("proprietaireNom", nomActuel);

            intent.putExtra("emailEtudiant", emailContact);
            intent.putExtra("etudiantEmail", emailContact);
            intent.putExtra("etudiantNom", nom);

        } else {

            intent = new Intent(
                    Interface21.this,
                    ChatEtudiant.class
            );

            intent.putExtra("emailEtudiant", emailActuel);
            intent.putExtra("etudiantEmail", emailActuel);
            intent.putExtra("etudiantNom", nomActuel);

            intent.putExtra("emailProprietaire", emailContact);
            intent.putExtra("proprietaireEmail", emailContact);
            intent.putExtra("proprietaireNom", nom);
        }

        intent.putExtra("conversationId", conversationId);
        intent.putExtra("nomContact", nom);
        intent.putExtra("emailContact", emailContact);

        intent.putExtra(
                "typeCompte",
                proprietaire ? "PROPRIETAIRE" : "ETUDIANT"
        );

        intent.putExtra(
                "typeUtilisateur",
                proprietaire ? "proprietaire" : "etudiant"
        );

        startActivity(intent);
    }

    // CONFIGURER LA RECHERCHE

    private void configurerRecherche() {

        if (edtRecherche == null) {
            return;
        }

        edtRecherche.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence s,
                    int start,
                    int count,
                    int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s,
                    int start,
                    int before,
                    int count) {

                filtrerConversations(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    // FILTRER LES CONVERSATIONS

    private void filtrerConversations(String recherche) {

        if (layoutConversations == null) {
            return;
        }

        String texte = recherche.trim().toLowerCase();

        for (int i = 0;
             i < layoutConversations.getChildCount();
             i++) {

            View carte = layoutConversations.getChildAt(i);

            if (carte instanceof LinearLayout) {

                LinearLayout layout = (LinearLayout) carte;

                if (layout.getChildCount() < 2) {
                    continue;
                }

                View informations = layout.getChildAt(1);

                if (!(informations instanceof LinearLayout)) {
                    continue;
                }

                LinearLayout info = (LinearLayout) informations;

                if (info.getChildCount() < 2) {
                    continue;
                }

                TextView tvNom = (TextView) info.getChildAt(0);
                TextView tvEmail = (TextView) info.getChildAt(1);

                String nom = tvNom.getText()
                        .toString()
                        .toLowerCase();

                String email = tvEmail.getText()
                        .toString()
                        .toLowerCase();

                boolean correspond =
                        nom.contains(texte)
                                || email.contains(texte);

                carte.setVisibility(
                        correspond ? View.VISIBLE : View.GONE
                );
            }
        }
    }

    private void appliquerRecherche() {

        if (edtRecherche != null) {
            filtrerConversations(
                    edtRecherche.getText().toString()
            );
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}