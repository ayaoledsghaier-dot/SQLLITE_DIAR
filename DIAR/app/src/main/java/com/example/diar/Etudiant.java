package com.example.diar;

public class Etudiant {
    private long id;
    private String nomComplet;
    private String email;
    private String universite;
    private int age;
    private double budgetMax;
    private String modeDeVie;
    private String nbColocataires;
    private String proprete;
    private String animauxAcceptes;
    private String quartier;
    private String faculte;
    private String photoUrl;
    private double pourcentageCompatible;
    private boolean favori;

    public Etudiant() {}

    public Etudiant(String nomComplet, String email, String universite, int age, double budgetMax, String modeDeVie, String nbColocataires, String proprete, String animauxAcceptes) {
        this.nomComplet = nomComplet;
        this.email = email;
        this.universite = universite;
        this.age = age;
        this.budgetMax = budgetMax;
        this.modeDeVie = modeDeVie;
        this.nbColocataires = nbColocataires;
        this.proprete = proprete;
        this.animauxAcceptes = animauxAcceptes;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getNomComplet() { return nomComplet; }
    public void setNomComplet(String nomComplet) { this.nomComplet = nomComplet; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUniversite() { return universite; }
    public void setUniversite(String universite) { this.universite = universite; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public double getBudgetMax() { return budgetMax; }
    public void setBudgetMax(double budgetMax) { this.budgetMax = budgetMax; }

    public String getModeDeVie() { return modeDeVie; }
    public void setModeDeVie(String modeDeVie) { this.modeDeVie = modeDeVie; }

    public String getNbColocataires() { return nbColocataires; }
    public void setNbColocataires(String nbColocataires) { this.nbColocataires = nbColocataires; }

    public String getProprete() { return proprete; }
    public void setProprete(String proprete) { this.proprete = proprete; }

    public String getAnimauxAcceptes() { return animauxAcceptes; }
    public void setAnimauxAcceptes(String animauxAcceptes) { this.animauxAcceptes = animauxAcceptes; }

    public String getQuartier() { return quartier; }
    public void setQuartier(String quartier) { this.quartier = quartier; }

    public String getFaculte() { return faculte; }
    public void setFaculte(String faculte) { this.faculte = faculte; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    // C'est ces 3 qui manquaient dans ton erreur
    public double getPourcentageCompatible() { return pourcentageCompatible; }
    public void setPourcentageCompatible(double pourcentageCompatible) { this.pourcentageCompatible = pourcentageCompatible; }

    public boolean isFavori() { return favori; }
    public boolean getFavori() { return favori; }
    public void setFavori(boolean favori) { this.favori = favori; }
}