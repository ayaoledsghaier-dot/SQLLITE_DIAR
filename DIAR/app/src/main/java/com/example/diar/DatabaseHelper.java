package com.example.diar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "diar.db";
    private static final int DATABASE_VERSION = 9;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createCoreTables(db);
        createAnnoncePhotoTable(db);
        createAuxiliaryTables(db);
    }

    // =====================================
    // TABLES PRINCIPALES
    // =====================================

    private void createCoreTables(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE IF NOT EXISTS Etudiant (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nomComplet TEXT," +
                "email TEXT UNIQUE," +
                "motDePasse TEXT," +
                "genre TEXT," +
                "age INTEGER," +
                "universite TEXT," +
                "faculte TEXT," +
                "ville TEXT," +
                "niveau TEXT," +
                "photoUrl TEXT," +
                "statutVerification TEXT," +
                "typeLogement TEXT," +
                "locationType TEXT," +
                "budgetMax REAL," +
                "equipements TEXT," +
                "quartier TEXT," +
                "modeDeVie TEXT," +
                "nbColocataires TEXT," +
                "animauxAcceptes TEXT," +
                "proprete TEXT," +
                "attestationPath TEXT)");

        db.execSQL("CREATE TABLE IF NOT EXISTS Proprietaire (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nom TEXT," +
                "email TEXT UNIQUE," +
                "motDePasse TEXT," +
                "photoPath TEXT)");

        db.execSQL("CREATE TABLE IF NOT EXISTS Annonce (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "titre TEXT," +
                "description TEXT," +
                "ville TEXT," +
                "adresse TEXT," +
                "quartier TEXT," +
                "nomProprietaire TEXT," +
                "type TEXT," +
                "prix REAL," +
                "nbPersonnes INTEGER," +
                "nbChambres INTEGER," +
                "surface REAL," +
                "latitude REAL," +
                "longitude REAL," +
                "disponible INTEGER DEFAULT 1," +
                "emailProprietaire TEXT)");
    }

    // =====================================
    // PHOTOS ANNONCES
    // =====================================

    private void createAnnoncePhotoTable(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE IF NOT EXISTS AnnoncePhoto (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "annonce_id INTEGER NOT NULL," +
                "photoPath TEXT," +
                "FOREIGN KEY(annonce_id) REFERENCES Annonce(id) ON DELETE CASCADE)");
    }

    // =====================================
    // TABLES SUPPLEMENTAIRES
    // =====================================

    private void createAuxiliaryTables(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE IF NOT EXISTS Favoris (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "emailEtudiant TEXT NOT NULL," +
                "annonce_id INTEGER NOT NULL," +
                "UNIQUE(emailEtudiant, annonce_id)," +
                "FOREIGN KEY(annonce_id) REFERENCES Annonce(id) ON DELETE CASCADE)");

        db.execSQL("CREATE TABLE IF NOT EXISTS FavorisColocataires (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "emailEtudiant TEXT NOT NULL," +
                "colocataire_id INTEGER NOT NULL," +
                "UNIQUE(emailEtudiant, colocataire_id)," +
                "FOREIGN KEY(colocataire_id) REFERENCES Etudiant(id) ON DELETE CASCADE)");

        db.execSQL("CREATE TABLE IF NOT EXISTS Reservation (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "annonce_id INTEGER NOT NULL," +
                "emailEtudiant TEXT NOT NULL," +
                "emailProprietaire TEXT NOT NULL," +
                "dateReservation TEXT," +
                "statut TEXT DEFAULT 'En attente'," +
                "FOREIGN KEY(annonce_id) REFERENCES Annonce(id) ON DELETE CASCADE)");

        db.execSQL("CREATE TABLE IF NOT EXISTS Conversation (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "emailEtudiant TEXT NOT NULL," +
                "emailProprietaire TEXT NOT NULL," +
                "nomEtudiant TEXT," +
                "nomProprietaire TEXT," +
                "dateCreation TEXT," +
                "UNIQUE(emailEtudiant, emailProprietaire))");

        db.execSQL("CREATE TABLE IF NOT EXISTS Message (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "conversation_id INTEGER NOT NULL," +
                "emailExpediteur TEXT NOT NULL," +
                "contenu TEXT NOT NULL," +
                "dateEnvoi TEXT," +
                "lu INTEGER DEFAULT 0," +
                "FOREIGN KEY(conversation_id) REFERENCES Conversation(id) ON DELETE CASCADE)");

        // NOTIFICATIONS ETUDIANT
        db.execSQL("CREATE TABLE IF NOT EXISTS Notification (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "emailEtudiant TEXT NOT NULL," +
                "titre TEXT," +
                "message TEXT," +
                "lu INTEGER DEFAULT 0," +
                "dateCreation INTEGER)");
    }

    // =====================================
    // MIGRATION SQLITE
    // =====================================

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.beginTransaction();

        try {

            createCoreTables(db);

            addColumnIfNotExists(db, "Etudiant", "nomComplet", "TEXT");
            addColumnIfNotExists(db, "Etudiant", "email", "TEXT");
            addColumnIfNotExists(db, "Etudiant", "motDePasse", "TEXT");
            addColumnIfNotExists(db, "Etudiant", "genre", "TEXT");
            addColumnIfNotExists(db, "Etudiant", "age", "INTEGER");
            addColumnIfNotExists(db, "Etudiant", "universite", "TEXT");
            addColumnIfNotExists(db, "Etudiant", "faculte", "TEXT");
            addColumnIfNotExists(db, "Etudiant", "ville", "TEXT");
            addColumnIfNotExists(db, "Etudiant", "niveau", "TEXT");
            addColumnIfNotExists(db, "Etudiant", "photoUrl", "TEXT");
            addColumnIfNotExists(db, "Etudiant", "statutVerification", "TEXT");
            addColumnIfNotExists(db, "Etudiant", "typeLogement", "TEXT");
            addColumnIfNotExists(db, "Etudiant", "locationType", "TEXT");
            addColumnIfNotExists(db, "Etudiant", "budgetMax", "REAL");
            addColumnIfNotExists(db, "Etudiant", "equipements", "TEXT");
            addColumnIfNotExists(db, "Etudiant", "quartier", "TEXT");
            addColumnIfNotExists(db, "Etudiant", "modeDeVie", "TEXT");
            addColumnIfNotExists(db, "Etudiant", "nbColocataires", "TEXT");
            addColumnIfNotExists(db, "Etudiant", "animauxAcceptes", "TEXT");
            addColumnIfNotExists(db, "Etudiant", "proprete", "TEXT");
            addColumnIfNotExists(db, "Etudiant", "attestationPath", "TEXT");

            addColumnIfNotExists(db, "Proprietaire", "nom", "TEXT");
            addColumnIfNotExists(db, "Proprietaire", "email", "TEXT");
            addColumnIfNotExists(db, "Proprietaire", "motDePasse", "TEXT");
            addColumnIfNotExists(db, "Proprietaire", "photoPath", "TEXT");

            addColumnIfNotExists(db, "Annonce", "titre", "TEXT");
            addColumnIfNotExists(db, "Annonce", "description", "TEXT");
            addColumnIfNotExists(db, "Annonce", "ville", "TEXT");
            addColumnIfNotExists(db, "Annonce", "adresse", "TEXT");
            addColumnIfNotExists(db, "Annonce", "quartier", "TEXT");
            addColumnIfNotExists(db, "Annonce", "nomProprietaire", "TEXT");
            addColumnIfNotExists(db, "Annonce", "type", "TEXT");
            addColumnIfNotExists(db, "Annonce", "prix", "REAL");
            addColumnIfNotExists(db, "Annonce", "nbPersonnes", "INTEGER");
            addColumnIfNotExists(db, "Annonce", "nbChambres", "INTEGER");
            addColumnIfNotExists(db, "Annonce", "surface", "REAL");
            addColumnIfNotExists(db, "Annonce", "latitude", "REAL");
            addColumnIfNotExists(db, "Annonce", "longitude", "REAL");
            addColumnIfNotExists(db, "Annonce", "disponible", "INTEGER DEFAULT 1");
            addColumnIfNotExists(db, "Annonce", "emailProprietaire", "TEXT");

            createAnnoncePhotoTable(db);
            createAuxiliaryTables(db);

            db.setTransactionSuccessful();

        } finally {
            db.endTransaction();
        }
    }

    private void addColumnIfNotExists(
            SQLiteDatabase db,
            String table,
            String column,
            String type) {

        Cursor cursor = db.rawQuery(
                "PRAGMA table_info(" + table + ")",
                null
        );

        boolean exists = false;

        try {
            while (cursor.moveToNext()) {
                String columnName = cursor.getString(
                        cursor.getColumnIndexOrThrow("name")
                );

                if (column.equalsIgnoreCase(columnName)) {
                    exists = true;
                    break;
                }
            }
        } finally {
            cursor.close();
        }

        if (!exists) {
            db.execSQL(
                    "ALTER TABLE " + table +
                            " ADD COLUMN " + column + " " + type
            );
        }
    }

    // =====================================
    // ETUDIANT
    // =====================================

    public boolean ajouterEtudiant(
            String nomComplet,
            String email,
            String motDePasse,
            String genre,
            String universite,
            String faculte,
            String ville,
            String niveau) {

        ContentValues values = new ContentValues();

        values.put("nomComplet", nomComplet);
        values.put("email", email);
        values.put("motDePasse", motDePasse);
        values.put("genre", genre);
        values.put("universite", universite);
        values.put("faculte", faculte);
        values.put("ville", ville);
        values.put("niveau", niveau);

        return getWritableDatabase().insert(
                "Etudiant", null, values
        ) != -1;
    }

    public boolean verifierConnexionEtudiant(
            String email,
            String motDePasse) {

        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT id FROM Etudiant WHERE email=? AND motDePasse=?",
                new String[]{email, motDePasse}
        );

        try {
            return cursor.moveToFirst();
        } finally {
            cursor.close();
        }
    }

    public Cursor getEtudiant(String email) {
        return getReadableDatabase().rawQuery(
                "SELECT * FROM Etudiant WHERE email=?",
                new String[]{email}
        );
    }

    public boolean updateProfilEtudiant(
            String email,
            String nom,
            String genre,
            String universite,
            String faculte,
            String ville,
            String niveau) {

        ContentValues values = new ContentValues();

        values.put("nomComplet", nom);
        values.put("genre", genre);
        values.put("universite", universite);
        values.put("faculte", faculte);
        values.put("ville", ville);
        values.put("niveau", niveau);

        return getWritableDatabase().update(
                "Etudiant", values, "email=?", new String[]{email}
        ) > 0;
    }

    public boolean enregistrerProfilEtudiant(
            String email,
            String genre,
            String universite,
            String niveau,
            String faculte,
            String ville) {

        ContentValues values = new ContentValues();

        values.put("genre", genre);
        values.put("universite", universite);
        values.put("niveau", niveau);
        values.put("faculte", faculte);
        values.put("ville", ville);

        return getWritableDatabase().update(
                "Etudiant", values, "email=?", new String[]{email}
        ) > 0;
    }

    public boolean updatePreferencesEtudiant(
            String email,
            String typeLogement,
            String locationType,
            double budgetMax,
            String equipements,
            String quartier) {

        ContentValues values = new ContentValues();

        values.put("typeLogement", typeLogement);
        values.put("locationType", locationType);
        values.put("budgetMax", budgetMax);
        values.put("equipements", equipements);
        values.put("quartier", quartier);

        return getWritableDatabase().update(
                "Etudiant", values, "email=?", new String[]{email}
        ) > 0;
    }

    public boolean updatePhotoEtudiant(String email, String photoPath) {

        ContentValues values = new ContentValues();
        values.put("photoUrl", photoPath);

        return getWritableDatabase().update(
                "Etudiant", values, "email=?", new String[]{email}
        ) > 0;
    }

    // =====================================
    // VERIFICATION ET ATTESTATION
    // =====================================

    public boolean enregistrerAttestation(String email, String path) {

        ContentValues values = new ContentValues();

        values.put("attestationPath", path);
        values.put("statutVerification", "En attente");

        return getWritableDatabase().update(
                "Etudiant", values, "email=?", new String[]{email}
        ) > 0;
    }

    public boolean modifierStatutVerification(String email, String statut) {

        ContentValues values = new ContentValues();
        values.put("statutVerification", statut);

        return getWritableDatabase().update(
                "Etudiant", values, "email=?", new String[]{email}
        ) > 0;
    }

    public Cursor getAttestationEtudiant(String email) {
        return getReadableDatabase().rawQuery(
                "SELECT attestationPath, statutVerification " +
                        "FROM Etudiant WHERE email=?",
                new String[]{email}
        );
    }

    public boolean changerMotDePasseEtudiant(
            String email,
            String ancien,
            String nouveau) {

        ContentValues values = new ContentValues();
        values.put("motDePasse", nouveau);

        return getWritableDatabase().update(
                "Etudiant",
                values,
                "email=? AND motDePasse=?",
                new String[]{email, ancien}
        ) > 0;
    }

    // =====================================
    // PROPRIETAIRE
    // =====================================

    public boolean ajouterProprietaire(
            String nom,
            String email,
            String motDePasse,
            String photoPath) {

        ContentValues values = new ContentValues();

        values.put("nom", nom);
        values.put("email", email);
        values.put("motDePasse", motDePasse);
        values.put("photoPath", photoPath);

        return getWritableDatabase().insert(
                "Proprietaire", null, values
        ) != -1;
    }

    public boolean verifierConnexionProprietaire(
            String email,
            String motDePasse) {

        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT id FROM Proprietaire WHERE email=? AND motDePasse=?",
                new String[]{email, motDePasse}
        );

        try {
            return cursor.moveToFirst();
        } finally {
            cursor.close();
        }
    }

    public Cursor getProprietaire(String email) {
        return getReadableDatabase().rawQuery(
                "SELECT * FROM Proprietaire WHERE email=?",
                new String[]{email}
        );
    }

    public boolean updatePhotoProprietaire(String email, String photoPath) {

        ContentValues values = new ContentValues();
        values.put("photoPath", photoPath);

        return getWritableDatabase().update(
                "Proprietaire", values, "email=?", new String[]{email}
        ) > 0;
    }

    public boolean updateProfilProprietaire(
            String email,
            String nouveauNom) {

        ContentValues values = new ContentValues();
        values.put("nom", nouveauNom);

        return getWritableDatabase().update(
                "Proprietaire", values, "email=?", new String[]{email}
        ) > 0;
    }

    public boolean changerMotDePasseProprietaire(
            String email,
            String ancien,
            String nouveau) {

        ContentValues values = new ContentValues();
        values.put("motDePasse", nouveau);

        return getWritableDatabase().update(
                "Proprietaire",
                values,
                "email=? AND motDePasse=?",
                new String[]{email, ancien}
        ) > 0;
    }

    // =====================================
    // ANNONCE
    // =====================================

    public long ajouterAnnonce(
            String titre,
            String description,
            String ville,
            String adresse,
            String quartier,
            String nomProprietaire,
            String type,
            double prix,
            int nbPersonnes,
            int nbChambres,
            double surface,
            double latitude,
            double longitude,
            String emailProprietaire) {

        ContentValues values = new ContentValues();

        values.put("titre", titre);
        values.put("description", description);
        values.put("ville", ville);
        values.put("adresse", adresse);
        values.put("quartier", quartier);
        values.put("nomProprietaire", nomProprietaire);
        values.put("type", type);
        values.put("prix", prix);
        values.put("nbPersonnes", nbPersonnes);
        values.put("nbChambres", nbChambres);
        values.put("surface", surface);
        values.put("latitude", latitude);
        values.put("longitude", longitude);
        values.put("disponible", 1);
        values.put("emailProprietaire", emailProprietaire);

        return getWritableDatabase().insert(
                "Annonce", null, values
        );
    }

    public Cursor getToutesLesAnnonces() {
        return getReadableDatabase().rawQuery(
                "SELECT * FROM Annonce WHERE disponible=1 ORDER BY id DESC",
                null
        );
    }

    public Cursor getAnnoncesProprietaire(String email) {
        return getReadableDatabase().rawQuery(
                "SELECT * FROM Annonce WHERE LOWER(TRIM(emailProprietaire))=? ORDER BY id DESC",
                new String[]{normaliserEmail(email)}
        );
    }

    public Cursor getAnnonce(long id) {
        return getReadableDatabase().rawQuery(
                "SELECT * FROM Annonce WHERE id=?",
                new String[]{String.valueOf(id)}
        );
    }

    public boolean supprimerAnnonce(long annonceId) {
        return getWritableDatabase().delete(
                "Annonce", "id=?", new String[]{String.valueOf(annonceId)}
        ) > 0;
    }

    public boolean modifierDisponibiliteAnnonce(
            long annonceId,
            boolean disponible) {

        ContentValues values = new ContentValues();
        values.put("disponible", disponible ? 1 : 0);

        return getWritableDatabase().update(
                "Annonce",
                values,
                "id=?",
                new String[]{String.valueOf(annonceId)}
        ) > 0;
    }

    // =====================================
    // PHOTOS ANNONCES
    // =====================================

    public boolean ajouterPhotoAnnonce(long annonceId, String photoPath) {

        ContentValues values = new ContentValues();

        values.put("annonce_id", annonceId);
        values.put("photoPath", photoPath);

        return getWritableDatabase().insert(
                "AnnoncePhoto", null, values
        ) != -1;
    }

    public Cursor getPhotosAnnonce(long annonceId) {
        return getReadableDatabase().rawQuery(
                "SELECT * FROM AnnoncePhoto WHERE annonce_id=?",
                new String[]{String.valueOf(annonceId)}
        );
    }

    // =====================================
    // FAVORIS LOGEMENTS
    // =====================================

    public boolean ajouterFavori(String emailEtudiant, long annonceId) {

        ContentValues values = new ContentValues();

        values.put("emailEtudiant", emailEtudiant);
        values.put("annonce_id", annonceId);

        return getWritableDatabase().insertWithOnConflict(
                "Favoris",
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
        ) != -1;
    }

    public boolean supprimerFavori(String emailEtudiant, long annonceId) {
        return getWritableDatabase().delete(
                "Favoris",
                "emailEtudiant=? AND annonce_id=?",
                new String[]{emailEtudiant, String.valueOf(annonceId)}
        ) > 0;
    }

    public Cursor getFavoris(String emailEtudiant) {
        return getReadableDatabase().rawQuery(
                "SELECT Annonce.* FROM Annonce " +
                        "INNER JOIN Favoris ON Annonce.id=Favoris.annonce_id " +
                        "WHERE Favoris.emailEtudiant=?",
                new String[]{emailEtudiant}
        );
    }

    // =====================================
    // FAVORIS COLOCATAIRES
    // =====================================

    public boolean ajouterFavoriColocataire(
            String emailEtudiant,
            long colocataireId) {

        if (emailEtudiant == null ||
                emailEtudiant.trim().isEmpty() ||
                colocataireId <= 0) {
            return false;
        }

        ContentValues values = new ContentValues();

        values.put("emailEtudiant", emailEtudiant);
        values.put("colocataire_id", colocataireId);

        return getWritableDatabase().insertWithOnConflict(
                "FavorisColocataires",
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
        ) != -1;
    }

    public boolean supprimerFavoriColocataire(
            String emailEtudiant,
            long colocataireId) {

        return getWritableDatabase().delete(
                "FavorisColocataires",
                "emailEtudiant=? AND colocataire_id=?",
                new String[]{emailEtudiant, String.valueOf(colocataireId)}
        ) > 0;
    }

    public boolean estFavoriColocataire(
            String emailEtudiant,
            long colocataireId) {

        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT id FROM FavorisColocataires " +
                        "WHERE emailEtudiant=? AND colocataire_id=?",
                new String[]{emailEtudiant, String.valueOf(colocataireId)}
        );

        try {
            return cursor.moveToFirst();
        } finally {
            cursor.close();
        }
    }

    public Cursor getFavorisColocataires(String emailEtudiant) {

        return getReadableDatabase().rawQuery(
                "SELECT " +
                        "Etudiant.id AS id, " +
                        "Etudiant.nomComplet AS nomComplet, " +
                        "Etudiant.email AS email, " +
                        "Etudiant.universite AS universite, " +
                        "Etudiant.faculte AS faculte, " +
                        "Etudiant.age AS age, " +
                        "Etudiant.budgetMax AS budgetMax, " +
                        "Etudiant.quartier AS quartier, " +
                        "Etudiant.modeDeVie AS modeDeVie, " +
                        "Etudiant.nbColocataires AS nbColocataires, " +
                        "Etudiant.animauxAcceptes AS animauxAcceptes, " +
                        "Etudiant.proprete AS proprete, " +
                        "Etudiant.photoUrl AS photoUrl " +
                        "FROM Etudiant " +
                        "INNER JOIN FavorisColocataires " +
                        "ON Etudiant.id=FavorisColocataires.colocataire_id " +
                        "WHERE FavorisColocataires.emailEtudiant=? " +
                        "ORDER BY FavorisColocataires.id DESC",
                new String[]{emailEtudiant}
        );
    }

    // =====================================
    // RESERVATIONS
    // =====================================

    private String normaliserEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    public long ajouterReservation(
            long annonceId,
            String emailEtudiant,
            String emailProprietaire,
            String dateReservation) {

        String etudiant = normaliserEmail(emailEtudiant);
        String proprietaire = normaliserEmail(emailProprietaire);

        if (annonceId <= 0 ||
                etudiant.isEmpty() ||
                proprietaire.isEmpty() ||
                dateReservation == null ||
                dateReservation.trim().isEmpty() ||
                etudiant.equals(proprietaire)) {
            return -1;
        }

        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT id FROM Annonce " +
                        "WHERE id=? AND disponible=1 " +
                        "AND LOWER(TRIM(emailProprietaire))=?",
                new String[]{
                        String.valueOf(annonceId),
                        proprietaire
                }
        );

        boolean annonceValide;

        try {
            annonceValide = cursor.moveToFirst();
        } finally {
            cursor.close();
        }

        if (!annonceValide) {
            return -1;
        }

        ContentValues values = new ContentValues();

        values.put("annonce_id", annonceId);
        values.put("emailEtudiant", etudiant);
        values.put("emailProprietaire", proprietaire);
        values.put("dateReservation", dateReservation.trim());
        values.put("statut", "En attente");

        return getWritableDatabase().insert(
                "Reservation",
                null,
                values
        );
    }

    public Cursor getReservationsEtudiant(String email) {

        String emailNormalise = normaliserEmail(email);

        if (emailNormalise.isEmpty()) {
            return null;
        }

        return getReadableDatabase().rawQuery(
                "SELECT * FROM Reservation " +
                        "WHERE LOWER(TRIM(emailEtudiant))=? " +
                        "ORDER BY id DESC",
                new String[]{emailNormalise}
        );
    }

    public Cursor getReservationsProprietaire(String email) {

        String emailNormalise = normaliserEmail(email);

        if (emailNormalise.isEmpty()) {
            return null;
        }

        return getReadableDatabase().rawQuery(
                "SELECT * FROM Reservation " +
                        "WHERE LOWER(TRIM(emailProprietaire))=? " +
                        "ORDER BY id DESC",
                new String[]{emailNormalise}
        );
    }

    public boolean modifierStatutReservation(
            long reservationId,
            String nouveauStatut) {

        if (reservationId <= 0 || nouveauStatut == null) {
            return false;
        }

        if (!nouveauStatut.equals("Acceptée") &&
                !nouveauStatut.equals("Refusée")) {
            return false;
        }

        ContentValues values = new ContentValues();
        values.put("statut", nouveauStatut);

        boolean ok = getWritableDatabase().update(
                "Reservation",
                values,
                "id=? AND statut=?",
                new String[]{
                        String.valueOf(reservationId),
                        "En attente"
                }
        ) > 0;

        // Quand le propriétaire accepte ou refuse, l'étudiant reçoit une notification
        if (ok) {
            creerNotificationReservation(reservationId, nouveauStatut);
        }

        return ok;
    }

    // =====================================
    // NOTIFICATIONS ETUDIANT
    // =====================================

    private void creerNotificationReservation(
            long reservationId,
            String statut) {

        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT r.emailEtudiant, r.dateReservation, a.titre " +
                        "FROM Reservation r " +
                        "LEFT JOIN Annonce a ON a.id = r.annonce_id " +
                        "WHERE r.id=?",
                new String[]{String.valueOf(reservationId)}
        );

        try {
            if (!cursor.moveToFirst()) {
                return;
            }

            String emailEtudiant = cursor.getString(0);
            String date = cursor.getString(1);
            String titreAnnonce = cursor.getString(2);

            if (titreAnnonce == null || titreAnnonce.trim().isEmpty()) {
                titreAnnonce = "le logement";
            }

            String titre;
            String message;

            if (statut.equals("Acceptée")) {
                titre = "Demande acceptée ✅";
                message = "Le propriétaire a accepté ta demande de visite pour \""
                        + titreAnnonce + "\""
                        + (date == null || date.trim().isEmpty()
                        ? "." : " le " + date + ".");
            } else {
                titre = "Demande refusée ❌";
                message = "Le propriétaire a refusé ta demande de visite pour \""
                        + titreAnnonce + "\".";
            }

            ajouterNotification(emailEtudiant, titre, message);

        } finally {
            cursor.close();
        }
    }

    public long ajouterNotification(
            String emailEtudiant,
            String titre,
            String message) {

        ContentValues values = new ContentValues();

        values.put("emailEtudiant", normaliserEmail(emailEtudiant));
        values.put("titre", titre);
        values.put("message", message);
        values.put("lu", 0);
        values.put("dateCreation", System.currentTimeMillis());

        return getWritableDatabase().insert(
                "Notification", null, values
        );
    }

    public Cursor getNotificationsEtudiant(String email) {

        return getReadableDatabase().rawQuery(
                "SELECT * FROM Notification " +
                        "WHERE LOWER(TRIM(emailEtudiant))=? " +
                        "ORDER BY id DESC",
                new String[]{normaliserEmail(email)}
        );
    }

    public int compterNotificationsNonLues(String email) {

        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM Notification " +
                        "WHERE LOWER(TRIM(emailEtudiant))=? AND lu=0",
                new String[]{normaliserEmail(email)}
        );

        try {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        } finally {
            cursor.close();
        }
    }

    public boolean marquerNotificationLue(long notificationId) {

        ContentValues values = new ContentValues();
        values.put("lu", 1);

        return getWritableDatabase().update(
                "Notification",
                values,
                "id=?",
                new String[]{String.valueOf(notificationId)}
        ) > 0;
    }

    public boolean marquerToutesNotificationsLues(String email) {

        ContentValues values = new ContentValues();
        values.put("lu", 1);

        return getWritableDatabase().update(
                "Notification",
                values,
                "LOWER(TRIM(emailEtudiant))=? AND lu=0",
                new String[]{normaliserEmail(email)}
        ) > 0;
    }

    // =====================================
    // CONVERSATIONS
    // =====================================

    public long creerConversation(
            String emailEtudiant,
            String emailProprietaire,
            String nomEtudiant,
            String nomProprietaire,
            String dateCreation) {

        ContentValues values = new ContentValues();

        values.put("emailEtudiant", emailEtudiant);
        values.put("emailProprietaire", emailProprietaire);
        values.put("nomEtudiant", nomEtudiant);
        values.put("nomProprietaire", nomProprietaire);
        values.put("dateCreation", dateCreation);

        long result = getWritableDatabase().insertWithOnConflict(
                "Conversation",
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
        );

        if (result == -1) {

            Cursor cursor = getReadableDatabase().rawQuery(
                    "SELECT id FROM Conversation " +
                            "WHERE emailEtudiant=? AND emailProprietaire=?",
                    new String[]{emailEtudiant, emailProprietaire}
            );

            try {
                if (cursor.moveToFirst()) {
                    result = cursor.getLong(0);
                }
            } finally {
                cursor.close();
            }
        }

        return result;
    }

    public Cursor getConversationsEtudiant(String email) {
        return getReadableDatabase().rawQuery(
                "SELECT * FROM Conversation WHERE emailEtudiant=? ORDER BY id DESC",
                new String[]{email}
        );
    }

    public Cursor getConversationsProprietaire(String email) {
        return getReadableDatabase().rawQuery(
                "SELECT * FROM Conversation WHERE emailProprietaire=? ORDER BY id DESC",
                new String[]{email}
        );
    }

    // =====================================
    // MESSAGES
    // =====================================

    public long envoyerMessage(
            long conversationId,
            String emailExpediteur,
            String contenu,
            String dateEnvoi) {

        ContentValues values = new ContentValues();

        values.put("conversation_id", conversationId);
        values.put("emailExpediteur", emailExpediteur);
        values.put("contenu", contenu);
        values.put("dateEnvoi", dateEnvoi);

        return getWritableDatabase().insert(
                "Message", null, values
        );
    }

    public Cursor getMessages(long conversationId) {
        return getReadableDatabase().rawQuery(
                "SELECT * FROM Message WHERE conversation_id=? ORDER BY id ASC",
                new String[]{String.valueOf(conversationId)}
        );
    }

    public boolean marquerMessagesLus(long conversationId) {

        ContentValues values = new ContentValues();
        values.put("lu", 1);

        return getWritableDatabase().update(
                "Message",
                values,
                "conversation_id=?",
                new String[]{String.valueOf(conversationId)}
        ) > 0;
    }
}