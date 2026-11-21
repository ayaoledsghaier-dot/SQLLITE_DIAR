
package com.example.diar;

public class Etudiant {

    private long id;

    private String nomComplet;
    private String email;

    private int age;

    private String universite;
    private String faculte;
    private String ville;
    private String niveau;

    private double budgetMax;
    private String quartier;

    private int pourcentage;

    private boolean favori;

    private String proprete;
    private String modeDeVie;
    private String nbColocataires;
    private String animauxAcceptes;

    private String photoUrl;

    private String typeLogement;
    private String locationType;

    public Etudiant() {
    }

    public Etudiant(
            String nomComplet,
            String email,
            String universite,
            int age,
            double budgetMax,
            String modeDeVie,
            String nbColocataires,
            String proprete,
            String animauxAcceptes
    ) {

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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNomComplet() {
        return nomComplet;
    }

    public void setNomComplet(String nomComplet) {
        this.nomComplet = nomComplet;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getUniversite() {
        return universite;
    }

    public void setUniversite(String universite) {
        this.universite = universite;
    }

    public String getFaculte() {
        return faculte;
    }

    public void setFaculte(String faculte) {
        this.faculte = faculte;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public String getNiveau() {
        return niveau;
    }

    public void setNiveau(String niveau) {
        this.niveau = niveau;
    }

    public double getBudgetMax() {
        return budgetMax;
    }

    public void setBudgetMax(double budgetMax) {
        this.budgetMax = budgetMax;
    }

    public String getQuartier() {
        return quartier;
    }

    public void setQuartier(String quartier) {
        this.quartier = quartier;
    }

    public int getPourcentage() {
        return pourcentage;
    }

    public void setPourcentage(int pourcentage) {
        this.pourcentage = pourcentage;
    }

    public Double getPourcentageCompatible() {
        return (double) pourcentage;
    }

    public void setPourcentageCompatible(Double pourcentage) {
        this.pourcentage = pourcentage == null
                ? 0
                : pourcentage.intValue();
    }

    public boolean isFavori() {
        return favori;
    }

    public void setFavori(boolean favori) {
        this.favori = favori;
    }

    public String getProprete() {
        return proprete;
    }

    public void setProprete(String proprete) {
        this.proprete = proprete;
    }

    public String getModeDeVie() {
        return modeDeVie;
    }

    public void setModeDeVie(String modeDeVie) {
        this.modeDeVie = modeDeVie;
    }

    public String getNbColocataires() {
        return nbColocataires;
    }

    public void setNbColocataires(String nbColocataires) {
        this.nbColocataires = nbColocataires;
    }

    public String getAnimauxAcceptes() {
        return animauxAcceptes;
    }

    public void setAnimauxAcceptes(String animauxAcceptes) {
        this.animauxAcceptes = animauxAcceptes;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getTypeLogement() {
        return typeLogement;
    }

    public void setTypeLogement(String typeLogement) {
        this.typeLogement = typeLogement;
    }

    public String getLocationType() {
        return locationType;
    }

    public void setLocationType(String locationType) {
        this.locationType = locationType;
    }
}
