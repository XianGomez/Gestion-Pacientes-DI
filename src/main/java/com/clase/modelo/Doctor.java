package com.clase.modelo;
import java.time.LocalDate;

public class Doctor {
    private String iddoc;
    private String apeldoc;
    private String nomedoc;
    private String movildoc;
    private String maildoc;
    private String espedoc;
    

    public Doctor(String iddoc, String apeldoc, String nomedoc,
            String movildoc, String maildoc, String espedoc) {
        this.iddoc = iddoc;
        this.apeldoc = apeldoc;
        this.nomedoc = nomedoc;
        this.movildoc = movildoc;
        this.maildoc = maildoc;
    }

    public String getIddoc() {
        return iddoc;
    }

    public void setIddoc(String iddoc) {
        this.iddoc = iddoc;
    }

    public String getApeldoc() {
        return apeldoc;
    }

    public void setApeldoc(String apeldoc) {
        this.apeldoc = apeldoc;
    }

    public String getNomedoc() {
        return nomedoc;
    }

    public void setNomedoc(String nomedoc) {
        this.nomedoc = nomedoc;
    }

    public String getMovildoc() {
        return movildoc;
    }

    public void setMovildoc(String movildoc) {
        this.movildoc = movildoc;
    }

    public String getEspedoc() {
        return espedoc;
    }

    public void setEspedoc(String espedoc) {
        this.espedoc = espedoc;
    }

    public String getMaildoc() {
        return maildoc;
    }

    public void setMaildoc(String maildoc) {
        this.maildoc = maildoc;
    }
}