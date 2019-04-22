package com.autenticar.tuid.model;

import android.app.Activity;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.autenticar.tuid.utils.HelperFunctions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class Persona {

    //FRENTE
    private static final String SURNAME = "Surname";
    private static final String NOMBRE = "Nombre";
    private static final String FECHA_DE_NACIMIENTO = "Fecha de Nacimiento";
    private static final String TRAMITE = "Trámite";
    private static final String DOCUMENTO = "Documento";
    private static final String SEXO = "Sexo";
    //DORSO
    private static final String DOMICILIO = "DOMICILIO";
    private static final String LUGAR_DE_NACIMIENTO = "LUGAR DE N";
    private static final String FECHA_DE_INGRESO = "FECHA DE INGRESO";
    private static final String DIGITO_PUGLAR = "PUGLAR";

    public String EMail;
    public String ApellidoQR; //corresponde a lectura de PDF147
    public String NombreQR;
    public String DocumentoQR;
    public String DocumentoOCRFront;// documento formateado
    public String TramiteQR;


    public String DocumentoPDF;
    public String FechaNacPDF;
    public Date FechaVecPDF;
    public String SexoPDF;

    public Date FechaNacQR;
    public String FechaVecQR;
    public String FechaNacimientoOCRFront;
    public boolean EsMasculinoQR = false;

    public int PUNTO_1 = 0;
    public int PUNTO_2 = 0;
    public int PUNTO_3 = 0;
    public int PUNTO_4 = 0;
    /*
        FORMATO EVALUACION PDF FRONT
    */
    public static final String PDF_APELLIDOS = "APELLIDO/S";
    public static final String PDF_NOMBRES = "NOMBRE/S";
    public static final String PDF_N_DOCUMENTO = "DE DOCUMENTO";
    public static final String PDF_SEXO = "SEXO";
    public static final String PDF_NACIONALIDAD = "NACIONALIDAD";
    public static final String PDF_F_EXPEDICION = "FECHA DE EXPEDICION";
    public static final String PDF_F_VENCIMIENTO = "FECHA DE VENCIMIENTO";
    /*
        FORMATO EVALUACION PDF BACK
    */
    public static final String PDF_DOMICILIO = "DOMICILIO";
    public static final String PDF_FECHA_LUGAR_NACIMIENTO = "FECHA";
    public static final String PDF_N_TRAMITE = "TRAMITE";
    public static final String PDF_OF_IDENT = "OF";


    public boolean ApellidoFrontPDFVerified;
    public boolean NombreFrontPDFVerified;
    public boolean DocumentoFrontPDFVerified;
    public boolean SexoFrontPDFVerified;
    public boolean FechaNacimientoFrontPDFVerified;
    public boolean FechaExpedicionFrontPDFVerified;
    public boolean FechaVencimientodoFrontPDFVerified;
    public boolean NacionalidadFrontPDFVerified;

    //solo se consigue leyendo el DNI
    public String DomicilioOCR;
    public String NacionalidadOCR;

    //retornan el estado de validaciones entre el PDF147 y OCR
    //si OCR ok se retorna true

    public boolean ApellidoFrontOCRVerified = false ;
    public boolean ApellidoBackOCRVerified;
    public boolean NombreFrontOCRVerified = false;
    public boolean NombreBackOCRVerified;
    public boolean DomicilioOCRAsignado;
    public boolean FechaNacimientoFrontOCRVerified = false;
    public boolean DocumentoFrontOCRVerified;
    public boolean DocumentoBackOCRVerified;
    public boolean TramiteOCRVerified = false;

    public Bitmap dniFront;
    public Bitmap dniBack;
    public Bitmap seflie;
    public Bitmap bitmapFace;
    public int prog=0;
    private boolean barcodeReaded, ocrFrontReaded, ocrBackReaded, pdfFrontReaded, pdfBackReaded, faceDetected;

    private ProgressBar progressBar;
    private Activity activity;
    private TextView scan_guide;

    public Persona() {
    }

    public void setScan_guide(TextView scan_guide) {
        this.scan_guide = scan_guide;
    }
    public void iniProgressBar() {
        progressBar.setProgress(0);
    }
    public ProgressBar getProgressBar() {
        return progressBar;
    }

    public void setProgressBar(ProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public String FileNameToUpload(Boolean isFront, boolean isDebug) {
        if (isDebug) {
            return (isFront == null ? "99999999F_S" : (isFront ? "99999999F_F" : "99999999F_B"));
        } else {
            return this.DocumentoQR + (this.EsMasculinoQR ? "M" : "F") + "_" + (isFront == null ? "S" : (isFront ? "F" : "B"));
        }
    }

    public boolean IsBarcodeReaded() {
        return barcodeReaded;
    }

    public boolean IsFaceDetected() {
        return faceDetected;
    }

    public void setMaxProgressBar(int value){
        progressBar.setMax(value);
    }



    public double ProcessFrontOCRReaded(String value) {
        double valorCorrobardo = 0;
        double validaciones = 0;
        valorCorrobardo = 0;

        if (value.indexOf(this.TramiteQR) > 0 && !TramiteOCRVerified) {
            this.TramiteOCRVerified = true;
            progressBar.setProgress(prog++);
        }
        if(TramiteOCRVerified){
            valorCorrobardo += 1;
        }
        validaciones += 1;

        if (value.indexOf(this.NombreQR.toUpperCase()) > 0 && !NombreFrontOCRVerified) {
            this.NombreFrontOCRVerified = true;
            progressBar.setProgress(prog++);
        }
        if(NombreFrontOCRVerified){
            valorCorrobardo += 1;
        }
        validaciones += 1;

        if (value.indexOf(this.ApellidoQR.toUpperCase()) > 0 && !ApellidoFrontOCRVerified) {
            this.ApellidoFrontOCRVerified = true;
            progressBar.setProgress(prog++);
        }
        if(ApellidoFrontOCRVerified){
            valorCorrobardo += 1;
        }
        validaciones += 1;

        if (value.indexOf(this.DocumentoOCRFront) > 0 && !DocumentoFrontOCRVerified) {
            this.DocumentoFrontOCRVerified = true;
            progressBar.setProgress(prog++);
        }
        if(DocumentoFrontOCRVerified){
            valorCorrobardo += 1;
        }
        validaciones += 1;

        if ((value.indexOf(this.FechaNacimientoOCRFront) > 0 || FechaNacimientoOCRFront.length() > 0) && !FechaNacimientoFrontOCRVerified) {
            this.FechaNacimientoFrontOCRVerified = true;
            progressBar.setProgress(prog++);
        }
        if(FechaNacimientoFrontOCRVerified){
            valorCorrobardo += 1;
        }
        validaciones += 1;

        if (value.indexOf(this.NombreQR.toUpperCase()) > 0 && !NombreFrontOCRVerified) {
            this.NombreFrontOCRVerified = true;
            progressBar.setProgress(prog++);
        }
        if(NombreFrontOCRVerified){
            valorCorrobardo += 1;
        }
        validaciones += 1;

        if (value.indexOf(this.ApellidoQR.toUpperCase()) > 0 && !ApellidoFrontOCRVerified) {
            this.ApellidoFrontOCRVerified = true;
            progressBar.setProgress(prog++);
        }
        if(ApellidoFrontOCRVerified){
            valorCorrobardo += 1;
        }
        validaciones += 1;

        double result = valorCorrobardo / validaciones;
        if (result > 0.75 ) {
            prog = 0;
            ocrFrontReaded = true;
        }
        return result;
    }
    public double ProcessBackOCRReaded(String value) {
        value = chararterSpece(value);
        double valorCorrobardo = 0;
        double validaciones = 0;

        //procesar domicilio y nacionalidad
        int domicilioIndex = value.indexOf(DOMICILIO);
        int lugarNacimiento = value.indexOf(LUGAR_DE_NACIMIENTO);
        if (domicilioIndex >= 0 && lugarNacimiento > domicilioIndex && !DomicilioOCRAsignado) {
            this.DomicilioOCR = value.substring(domicilioIndex + DOMICILIO.length(), lugarNacimiento);
            this.DomicilioOCRAsignado = true;
            progressBar.setProgress(prog++);
            System.out.println("0");
        }
        if(DomicilioOCRAsignado){
            valorCorrobardo += 1;
        }
        validaciones += 1;

        String stringAValidar = this.NombreQR.toUpperCase().replace(" ", "K");
        String string2AValidar = this.NombreQR.toUpperCase().replace(" ", "");
        String string3AValidar = this.NombreQR.toUpperCase().replace(" ", "<");
        if (value.indexOf(stringAValidar) > 0 && !NombreBackOCRVerified) {
            this.NombreBackOCRVerified = true;
            progressBar.setProgress(prog++);
            System.out.println("1");
        } else if (value.indexOf(string2AValidar) > 0 && !NombreBackOCRVerified) {
            this.NombreBackOCRVerified = true;
            progressBar.setProgress(prog++);
            System.out.println("1");
        } else if (value.indexOf(string3AValidar) > 0 && !NombreBackOCRVerified) {
            this.NombreBackOCRVerified = true;
            progressBar.setProgress(prog++);
            System.out.println("1");
        }
        if (this.NombreBackOCRVerified) {
            valorCorrobardo += 1;
        }
        validaciones += 1;


        stringAValidar = this.ApellidoQR.toUpperCase().replace(" ", "K");
        string2AValidar = this.ApellidoQR.toUpperCase().replace(" ", "");
        string3AValidar = this.ApellidoQR.toUpperCase().replace(" ", "<");
        if (value.indexOf(stringAValidar) > 0 && !ApellidoBackOCRVerified) {
            this.ApellidoBackOCRVerified = true;
            progressBar.setProgress(prog++);
            System.out.println("2");
        } else if (value.indexOf(string2AValidar) > 0 && !ApellidoBackOCRVerified) {
            this.ApellidoBackOCRVerified = true;
            progressBar.setProgress(prog++);
            System.out.println("2");
        } else if (value.indexOf(string3AValidar) > 0 && !ApellidoBackOCRVerified) {
            this.ApellidoBackOCRVerified = true;
            progressBar.setProgress(prog++);
            System.out.println("2");
        }
        if (this.ApellidoBackOCRVerified) {
            valorCorrobardo += 1;
        }
        validaciones += 1;
        if (!DocumentoBackOCRVerified && DocumentoQR.length() > 0) {
            this.DocumentoBackOCRVerified = true;
            progressBar.setProgress(prog++);
            System.out.println("3");
        }else {
            valorCorrobardo += 1;
        }
        validaciones += 1;

        //return valorCorrobardo / validaciones;
        //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        /*Digito de control*/
        int determinador = 0;
        if (PUNTO_1 == 0) {
            int n_ini = value.indexOf("IDARG" + DocumentoQR + "<");
            int n_end = value.indexOf("<<");
            if (n_ini >= 0 && n_end >= 0) {
                String val = value.substring(n_ini + ("IDARG" + DocumentoQR + "<").length(), n_end).trim();
                if (Integer.valueOf(val) > 0 && Integer.valueOf(val) < 10) {
                    PUNTO_1 = Integer.valueOf(val);
                    progressBar.setProgress(prog++);
                    System.out.println("4");
                }
            }
        }
        if (PUNTO_2 == 0) {
            SimpleDateFormat formato = new SimpleDateFormat("yyMMdd");
            String FN = formato.format(FechaNacQR);
            int n_ini = value.indexOf(FN);
            if (n_ini >= 0) {
                String val = value.substring(n_ini + (FN).length(), n_ini + (FN).length() + 1).trim();
                if (Integer.valueOf(val.trim()) > 0 && Integer.valueOf(val.trim()) < 10) {
                    PUNTO_2 = Integer.valueOf(val);
                    progressBar.setProgress(prog++);
                    System.out.println("5");
                }
            }
        }
        if (PUNTO_3 == 0) {
            int n_end = value.indexOf("ARG<");
            if (n_end >= 0) {
                String val = value.substring(n_end - 1, n_end).trim();
                FechaVecQR = value.substring(n_end - 7, n_end-1).trim();
                if (Integer.valueOf(val) > 0 && Integer.valueOf(val) < 10) {
                    PUNTO_3 = Integer.valueOf(val);
                    PUNTO_4 = 1;
                    progressBar.setProgress(prog++);
                    System.out.println("6");
                }
            }
        }
       /* if (PUNTO_4 == 0) {
            int n_end = value.indexOf(ApellidoQR.toUpperCase() + "<");
            if (n_end >= 0) {
                String val = value.substring(n_end - 2, n_end).trim();
                if (Integer.valueOf(val) > 0 && Integer.valueOf(val) < 10) {
                    PUNTO_4 = Integer.valueOf(val);
                    progressBar.setProgress(prog++);
                }
            }
        }*/
        if(PUNTO_1>0 && PUNTO_2>0 && PUNTO_3>0 && PUNTO_4>0 && determinador==0){
            determinador=desmintificado_DNI();
        }
        //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

        double result = valorCorrobardo / validaciones;
        if (result > 0.75){
            if(determinador==1) {
                ocrBackReaded = true;
                prog = 0;
            }else{
                System.out.println("DNI no valido");
            }
        }
        return result;
    }
    public double ProcessFrontPDFReaded(String value) {
        double valorCorrobardo = 0;
        double validaciones = 0;

        valorCorrobardo = 0;

        if (value.indexOf(PDF_APELLIDOS.toUpperCase()) > 0 && !ApellidoFrontPDFVerified ) {
            this.ApellidoFrontPDFVerified = true;
            prog++;
            progressBar.setProgress(prog);
        }
        if(ApellidoFrontPDFVerified){
            valorCorrobardo += 1;
        }
        validaciones += 1;
        if (value.indexOf(PDF_NOMBRES.toUpperCase()) > 0 && !NombreFrontPDFVerified) {
            this.NombreFrontPDFVerified = true;
            prog++;
            progressBar.setProgress(prog);
        }
        if(NombreFrontPDFVerified){
            valorCorrobardo += 1;
        }
        validaciones += 1;
        if (value.indexOf(PDF_N_DOCUMENTO) > 0) {
            int ini = value.indexOf(PDF_N_DOCUMENTO);
            int fin = value.indexOf(PDF_NACIONALIDAD);
            if (ini >= 0 && fin > ini && !DocumentoFrontPDFVerified) {
                this.DocumentoPDF = value.substring(ini + PDF_N_DOCUMENTO.length(), fin).trim();
                this.DocumentoPDF = this.DocumentoPDF.replace(".", "");
                this.DocumentoFrontPDFVerified = true;
                prog++;
                progressBar.setProgress(prog);
            }
        }
        if(DocumentoFrontPDFVerified){
            valorCorrobardo += 1;
        }
        validaciones += 1;
        if (value.indexOf(PDF_F_VENCIMIENTO) > 0 ) {
            int ini = value.indexOf(PDF_F_VENCIMIENTO);
            int fin = value.indexOf("IDAR");
            if (ini >= 0 && fin > ini && !FechaVencimientodoFrontPDFVerified) {
                String FV = value.substring(ini + PDF_F_VENCIMIENTO.length()+1, fin);
                if(FV.length()==11){
                    this.FechaVecPDF = parseDateDDMMYYYY(FV);
                    this.FechaVencimientodoFrontPDFVerified = true;
                    prog++;
                    progressBar.setProgress(prog);
                }
            }
        }
        if(FechaVencimientodoFrontPDFVerified){
            valorCorrobardo += 1;
        }
        validaciones += 1;
        if (value.indexOf(PDF_SEXO) > 0 ) {
            int ini = value.indexOf(PDF_SEXO);
            int fin = value.indexOf("NO VALIDO");
            if (ini >= 0 && fin > ini && !SexoFrontPDFVerified) {
                this.SexoPDF =value.substring(ini + PDF_SEXO.length(), fin).trim();
                if(SexoPDF.length() == 1){
                    this.SexoFrontPDFVerified = true;
                    prog++;
                    progressBar.setProgress(prog);
                }
            }
        }
        if(SexoFrontPDFVerified){
            valorCorrobardo += 1;
        }
        validaciones += 1;
        if (value.indexOf(PDF_F_EXPEDICION) > 0 && !FechaExpedicionFrontPDFVerified) {
            this.FechaExpedicionFrontPDFVerified = true;
            prog++;
            progressBar.setProgress(prog);
        }
        if(FechaExpedicionFrontPDFVerified){
            valorCorrobardo += 1;
        }
        validaciones += 1;
        if (value.indexOf(PDF_NACIONALIDAD) > 0 && !FechaNacimientoFrontPDFVerified) {
            this.FechaNacimientoFrontPDFVerified = true;
            prog++;
            progressBar.setProgress(prog);
        }
        if(FechaNacimientoFrontPDFVerified){
            valorCorrobardo += 1;
        }
        validaciones += 1;


        //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        /*Digito de control*/
        int determinador = 0;
        if (PUNTO_1 == 0) {
            int n_ini = value.indexOf("IDARG" + DocumentoPDF + "<");
            int n_end = value.indexOf("<<");
            if (n_ini >= 0 && n_end >= 0 && n_ini<n_end) {
                String val = value.substring(n_ini + ("IDARG" + DocumentoPDF + "<").length(), n_end);
                if (Integer.valueOf(val) > 0 && Integer.valueOf(val) < 10) {
                    PUNTO_1 = Integer.valueOf(val);
                    this.DocumentoQR = DocumentoPDF;
                    prog++;
                    progressBar.setProgress(prog);
                }
            }
        }
        if (PUNTO_2 == 0) {
            SimpleDateFormat formato = new SimpleDateFormat("yyMMdd");
            String FN = formato.format(FechaVecPDF);
            int n_fin = value.indexOf(SexoPDF+FN);
            if (n_fin >= 0) {
                String val = value.substring(n_fin-1, n_fin).trim();
                if (Integer.valueOf(val.trim()) > 0 && Integer.valueOf(val.trim()) < 10) {
                    PUNTO_2 = Integer.valueOf(val);
                    FechaNacPDF = value.substring(n_fin-7, n_fin-1).trim();
                    prog++;
                    progressBar.setProgress(prog);
                }
            }
        }
        if (PUNTO_3 == 0) {
            SimpleDateFormat formato = new SimpleDateFormat("yyMMdd");
            String FN = formato.format(FechaVecPDF);
            int n_ini = value.indexOf(SexoPDF+FN);
            int n_end = value.indexOf("ARG<");
            if (n_ini >= 0 && n_end > n_ini) {
                String val = value.substring(n_ini + (SexoPDF + FN).length(), n_end);
                if (Integer.valueOf(val) > 0 && Integer.valueOf(val) < 10) {
                    PUNTO_3 = Integer.valueOf(val);
                    this.FechaVecQR = FN;
                    prog++;
                    progressBar.setProgress(prog);
                }
            }
        }
        /*
        if (PUNTO_4 == 0) {
            int n_end = value.indexOf(ApellidoQR.toUpperCase() + "<");
            if (n_end >= 0) {
                String val = value.substring(n_end - 2, n_end).trim();
                if (Integer.valueOf(val) > 0 && Integer.valueOf(val) < 10) {
                    PUNTO_4 = Integer.valueOf(val);
                }
            }
        }*/
        if(PUNTO_1 > 0 && PUNTO_2 > 0 && PUNTO_3 > 0 && determinador == 0){
            determinador=desmintificado_DNI();
            if(determinador > 0){
                prog++;
                progressBar.setProgress(prog);
            }
        }
        //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        double result = valorCorrobardo / validaciones;
        if (result > 0.75 && determinador > 0) {
            if(determinador == 1){
                pdfFrontReaded = true;
                prog = 0;
            }else{
                scan_guide.setText("El DNI no es valido");
            }
        }
        return result;
    }
    public double ProcessBackPDFReaded(String value_pdf, String value) {
        double valorCorrobardo = 0;
        double validaciones = 0;
        value = chararterSpece(value);
        value_pdf = chararterSpece(value_pdf);
        valorCorrobardo = 0;

        if (value_pdf.indexOf(this.NombreQR.toUpperCase()) > 0 && !NombreFrontOCRVerified) {
            this.NombreFrontOCRVerified = true;
            progressBar.setProgress(prog++);
        }
        if(NombreFrontOCRVerified){
            valorCorrobardo += 1;
        }
        validaciones += 1;

        if (value_pdf.indexOf(this.ApellidoQR.toUpperCase()) > 0 && !ApellidoFrontOCRVerified) {
            this.ApellidoFrontOCRVerified = true;
            progressBar.setProgress(prog++);
        }
        if(ApellidoFrontOCRVerified){
            valorCorrobardo += 1;
        }
        validaciones += 1;

        if (value_pdf.indexOf(this.DocumentoOCRFront) > 0 && !DocumentoFrontOCRVerified) {
            this.DocumentoFrontOCRVerified = true;
            progressBar.setProgress(prog++);
        }
        if(DocumentoFrontOCRVerified){
            valorCorrobardo += 1;
        }
        validaciones += 1;

        if (this.FechaNacimientoOCRFront.length() > 0 && !FechaNacimientoFrontOCRVerified) {
            this.FechaNacimientoFrontOCRVerified = true;
            progressBar.setProgress(prog++);
        }
        if(FechaNacimientoFrontOCRVerified){
            valorCorrobardo += 1;
        }
        validaciones += 1;

        if (this.DocumentoQR.length() > 0 && !DocumentoBackOCRVerified) {
            this.DocumentoBackOCRVerified = true;
            progressBar.setProgress(prog++);
        }
        if(DocumentoBackOCRVerified){
            valorCorrobardo += 1;
        }
        validaciones += 1;

        if (value_pdf.indexOf(this.NacionalidadOCR.toUpperCase()) > 0 && !NacionalidadFrontPDFVerified) {
            this.NacionalidadFrontPDFVerified = true;
            progressBar.setProgress(prog++);
        }
        if(NacionalidadFrontPDFVerified){
            valorCorrobardo += 1;
        }
        validaciones += 1;

        if(!DomicilioOCRAsignado){
            int domicilioIndex = value.indexOf(DOMICILIO);
            int lugarNacimiento = value.indexOf(PDF_FECHA_LUGAR_NACIMIENTO);
            if (domicilioIndex >= 0 && lugarNacimiento > domicilioIndex && !DomicilioOCRAsignado) {
                this.DomicilioOCR = value.substring(domicilioIndex + DOMICILIO.length(), lugarNacimiento);
                this.DomicilioOCRAsignado = true;
                progressBar.setProgress(prog++);
            }
        }else{
            valorCorrobardo += 1;
        }
        validaciones += 1;

        if(!TramiteOCRVerified){
            int n_ini = value.indexOf(PDF_N_TRAMITE);
            int n_end = value.indexOf(PDF_OF_IDENT);
            if (n_ini >= 0 && n_end > n_ini) {
                this.TramiteQR = value.substring(n_ini + PDF_N_TRAMITE.length(), n_end).trim();
                if (this.TramiteQR.length() == 11) {
                    this.TramiteOCRVerified = true;
                    progressBar.setProgress(prog++);
                }
            }
        }else{
            valorCorrobardo += 1;
        }
        validaciones += 1;

        double result = valorCorrobardo / validaciones;
        if (result > 0.75) {
            prog=0;
            pdfBackReaded = true;
        }
        return result;
    }



    public void SetBarcodeReaded() {
        barcodeReaded = true;
    }

    public void SetFaceDetected(Bitmap face) {
        faceDetected = true;
        bitmapFace = face;
    }

    public boolean IsOCRFrontReaded() {
        return ocrFrontReaded;
    }

    public boolean IsOCRBackReaded() {
        return ocrBackReaded;
    }

    public boolean IsPDFFrontReaded() {
        return pdfFrontReaded;
    }

    public boolean IsPDFBackReaded() {
        return pdfBackReaded;
    }

    public boolean IsFilled() {
        return (IsOCRBackReaded() || IsPDFBackReaded()) && IsFaceDetected() && (IsOCRFrontReaded() || IsPDFFrontReaded()) && dniFront != null && dniBack != null;
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder();
        ret.append("<b>Nombre:</b>");
        ret.append(this.NombreQR);
        ret.append("</br>");
        ret.append("<b>Apellido:</b>");
        ret.append(this.ApellidoQR);
        ret.append("</br>");
        ret.append("<b>Número Documento:</b>");
        ret.append(this.DocumentoQR);
        ret.append("</br>");
        ret.append("<b>Fecha de Nacimiento:</b>");
        ret.append(this.FechaNacQR);
        ret.append("</br>");
        ret.append("<b>Género:</b>");
        ret.append(this.EsMasculinoQR ? "Masculino" : "Femenino");
        ret.append("</br>");
        if (!TextUtils.isEmpty(this.DomicilioOCR)) {
            ret.append("<b>Domicilio:</b>");
            ret.append(this.DomicilioOCR);
            ret.append("</br>");
        }
        if (!TextUtils.isEmpty(this.DomicilioOCR)) {
            ret.append("<b>Nacionalidad:</b>");
            ret.append(this.NacionalidadOCR);
            ret.append("</br>");
        }
        return ret.toString();
    }

    public String toHTML() {
        StringBuilder ret = new StringBuilder();
        ret.append("<b>NombreQR:</b>");
        ret.append(this.NombreQR);
        ret.append("</br>");
        ret.append("<b>NombreFrontOCRVerified:</b>");
        ret.append(this.NombreFrontOCRVerified);
        ret.append("</br>");
        ret.append("<b>ApellidoQR:</b>");
        ret.append(this.ApellidoQR);
        ret.append("</br>");
        ret.append("<b>ApellidoFrontOCRVerified:</b>");
        ret.append(this.ApellidoFrontOCRVerified);
        ret.append("</br>");
        ret.append("<b>DocumentoQR:</b>");
        ret.append(this.DocumentoQR);
        ret.append("</br>");
        ret.append("<b>DocumentoFrontOCRVerified:</b>");
        ret.append(this.DocumentoFrontOCRVerified);
        ret.append("</br>");
        ret.append("<b>FechaNacQR:</b>");
        ret.append(this.FechaNacQR);
        ret.append("</br>");
        ret.append("<b>FechaNacimientoFrontOCRVerified:</b>");
        ret.append(this.FechaNacimientoFrontOCRVerified);
        ret.append("</br>");
        ret.append("<b>GéneroOR:</b>");
        ret.append(this.EsMasculinoQR ? "Masculino" : "Femenino");
        ret.append("</br>");
        ret.append("<b>TramiteQR:</b>");
        ret.append(this.TramiteQR);
        ret.append("</br>");
        ret.append("<b>TramiteOCRVerified:</b>");
        ret.append(this.TramiteOCRVerified);
        ret.append("</br>");

        if (!TextUtils.isEmpty(this.DomicilioOCR)) {
            ret.append("<b>Domicilio:</b>");
            ret.append(this.DomicilioOCR);
            ret.append("</br>");
        }
        if (!TextUtils.isEmpty(this.DomicilioOCR)) {
            ret.append("<b>Nacionalidad:</b>");
            ret.append(this.NacionalidadOCR);
            ret.append("</br>");
        }
        return ret.toString();
    }

    public Document GetRequest() {
        Document document = new Document();
        document.tramit = this.TramiteQR;
        document.number = this.DocumentoQR;
        document.lastname = this.ApellidoQR;
        document.gender = this.EsMasculinoQR ? "M" : "F";
        document.expires = "2026-05-05";
        document.birth = this.FechaNacQR;
        document.address = this.DomicilioOCR;
        document.name = this.NombreQR;
        document.back = HelperFunctions.encodeTobase64(this.dniBack);
        document.front = HelperFunctions.encodeTobase64(this.dniFront);
        return document;
    }

    public static String chararterSpece(String input) {
        String original = "áàäéèëíìïóòöúùuñÁÀÄÉÈËÍÌÏÓÒÖÚÙÜÑçÇ";
        String ascii = "aaaeeeiiiooouuunAAAEEEIIIOOOUUUNcC";
        String output = input;
        for (int i = 0; i < original.length(); i++) {
            output = output.replace(original.charAt(i), ascii.charAt(i));
        }
        return output;
    }

    public int desmintificado_DNI() {
        int vector[] = {7, 3, 1};
        int j=0;
        int count_1 = 0, count_2 = 0, count_3 = 0, count_4 = 0;

        for (int i = 0; i < DocumentoQR.length(); i++){
            count_1+= (Integer.parseInt(String.valueOf(DocumentoQR.charAt(i))))*vector[j];
            j++;
            if(j==3){
                j=0;
            }
        }
        if(PUNTO_1==count_1%10){
            String FN;
            if(FechaNacQR!=null){
                SimpleDateFormat formato = new SimpleDateFormat("yyMMdd");
                FN = formato.format(FechaNacQR);
            }else{
                FN = FechaNacPDF;
            }

            j = 0;
            for (int i = 0; i < FN.length(); i++){
                count_2+= (Integer.parseInt(String.valueOf(FN.charAt(i))))*vector[j];
                j++;
                if(j==3){
                    j=0;
                }
            }
            if(PUNTO_2==count_2%10){
                j = 0;
                for (int i = 0; i < FechaVecQR.length(); i++){
                    count_3+= (Integer.parseInt(String.valueOf(FechaVecQR.charAt(i))))*vector[j];
                    j++;
                    if(j==3){
                        j=0;
                    }
                }
                if(PUNTO_3==count_3%10) {
                    return 1;
                }else{
                    return 2;
                }
               /* if(PUNTO_3==count_3%10) {
                    System.out.println("Si es punto 3: "+count_3);
                    String val = DocumentoQR+String.valueOf(PUNTO_1)+FN+String.valueOf(PUNTO_2)+FechaVecQR;
                    j = 0;
                    for (int i = 0; i < val.length(); i++){
                        count_4+= (Integer.parseInt(String.valueOf(val.charAt(i))))*vector[j];
                        j++;
                        if(j==3){
                            j=0;
                        }
                    }
                    System.out.println("punto 4: "+count_4);
                    if(PUNTO_4==count_4%10) {
                        return 1;
                    }else{
                        return 2;
                    }
                }*/
            }
        }
        return 0;
    }
    private static Date parseDateDDMMYYYY(String date) {
        Calendar mCalendarFechaNac = new GregorianCalendar();
        String[] dates = date.split(" ");
        mCalendarFechaNac.set(Calendar.YEAR, Integer.parseInt(dates[2]));
        if(dates[1].equals("ENE")){
            mCalendarFechaNac.set(Calendar.MONTH, Calendar.JUNE);
        }else if(dates[1].equals("FEB")){
            mCalendarFechaNac.set(Calendar.MONTH, Calendar.FEBRUARY);
        }else if(dates[1].equals("MAR")){
            mCalendarFechaNac.set(Calendar.MONTH, Calendar.MARCH);
        }else if(dates[1].equals("APR")){
            mCalendarFechaNac.set(Calendar.MONTH, Calendar.APRIL);
        }else if(dates[1].equals("MAY")){
            mCalendarFechaNac.set(Calendar.MONTH, Calendar.MAY);
        }else if(dates[1].equals("JUN")){
            mCalendarFechaNac.set(Calendar.MONTH, Calendar.JUNE);
        }else if(dates[1].equals("JUL")){
            mCalendarFechaNac.set(Calendar.MONTH, Calendar.JULY);
        }else if(dates[1].equals("AGO")){
            mCalendarFechaNac.set(Calendar.MONTH, Calendar.AUGUST);
        }else if(dates[1].equals("SEP")){
            mCalendarFechaNac.set(Calendar.MONTH, Calendar.SEPTEMBER);
        }else if(dates[1].equals("OCT")){
            mCalendarFechaNac.set(Calendar.MONTH, Calendar.OCTOBER);
        }else if(dates[1].equals("NOV")){
            mCalendarFechaNac.set(Calendar.MONTH, Calendar.NOVEMBER);
        }else if(dates[1].equals("DIC")){
            mCalendarFechaNac.set(Calendar.MONTH, Calendar.DECEMBER);
        }
        mCalendarFechaNac.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dates[0]));
        return mCalendarFechaNac.getTime();
    }
}
