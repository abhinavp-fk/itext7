package com.itextpdf.core.pdf;

import com.itextpdf.core.exceptions.PdfException;

import java.io.IOException;

abstract public class PdfObject {

    static public final byte Array = 1;
    static public final byte Boolean = 2;
    static public final byte Dictionary = 3;
    static public final byte IndirectReference = 4;
    static public final byte Name = 5;
    static public final byte Null = 6;
    static public final byte Number = 7;
    static public final byte Stream = 8;
    static public final byte String = 9;


    /**
     * If object is flushed the indirect reference is kept here.
     */
    protected PdfIndirectReference indirectReference = null;

    public PdfObject() {

    }

    /**
     * Gets object type.
     *
     * @return object type.
     */
    abstract public byte getType();

    /**
     * Flushes the object to the document.
     *
     * @throws PdfException
     */
    final public void flush() throws PdfException {
        flush(true);
    }

    /**
     * Flushes the object to the document.
     *
     * @param canBeInObjStm indicates whether object can be placed into object stream.
     * @throws PdfException
     */
    final public void flush(boolean canBeInObjStm) throws PdfException {
        try {
            PdfWriter writer = getWriter();
            if (writer != null)
                writer.flushObject(this, getType() != Stream && getType() != IndirectReference && canBeInObjStm);
        } catch (IOException e) {
            throw new PdfException(PdfException.CannotFlushObject, e, this);
        }
    }

    /**
     * Copied object to a specified document.
     *
     * @param document document to copy object to.
     * @return copied object.
     */
    public <T extends PdfObject> T copy(PdfDocument document) throws PdfException {
        return copy(document, true);
    }

    /**
     * Copies object.
     *
     * @return copied object.
     */
    public <T extends PdfObject> T copy() throws PdfException {
        return copy(getDocument());
    }

    /**
     * Gets the indirect reference associated with the object.
     * The indirect reference is used when flushing object to the document.
     * If no reference is associated - create a new one.
     *
     * @return indirect reference.
     */
    public PdfIndirectReference getIndirectReference() {
        return indirectReference;
    }

    /**
     * Marks object to be saved as indirect.
     *
     * @param document a document the indirect reference will belong to.
     * @return object itself.
     */
    public <T extends PdfObject> T makeIndirect(PdfDocument document) {
        setDocument(document);
        return (T) this;
    }

    /**
     * Indicates is the object has been flushed or not.
     *
     * @return true is object has been flushed, otherwise false.
     */
    public boolean isFlushed() {
        PdfIndirectReference indirectReference = getIndirectReference();
        return (indirectReference != null && indirectReference.flushed);
    }

    /**
     * Gets the document the object belongs to.
     *
     * @return a document the object belongs to. If object is direct return null.
     */
    public PdfDocument getDocument() {
        if (indirectReference != null)
            return indirectReference.getDocument();
        return null;
    }

    /**
     * Sets PdfDocument for the object.
     *
     * @param document a dPdfDocument to set.
     */
    public void setDocument(PdfDocument document) {
        if (document != null && indirectReference == null) {
            indirectReference = document.getNextIndirectReference(this);
            document.getXRef().add(indirectReference);
        }
    }

    /**
     * Copied object to a specified document.
     *
     * @param document         document to copy object to.
     * @param allowDuplicating indicates if to allow copy objects which already have been copied.
     *                         If object is associated with any indirect reference and allowDuplicating is false then already existing reference will be returned instead of copying object.
     *                         If allowDuplicating is true then object will be copied and new indirect reference will be assigned.
     * @return copied object.
     * @throws PdfException
     */
    protected <T extends PdfObject> T copy(PdfDocument document, boolean allowDuplicating) throws PdfException {
        if (isFlushed())
            throw new PdfException(PdfException.CannotCopyFlushedObject);
        PdfWriter writer = null;
        if (document != null)
            writer = document.getWriter();
        if (writer != null)
            return (T) writer.copyObject(this, document, allowDuplicating);
        T newObject = newInstance();
        newObject.copyContent(this, document);
        return newObject;
    }

    /**
     * Gets a PdfWriter associated with the document object belongs to.
     *
     * @return PdfWriter.
     */
    protected PdfWriter getWriter() {
        PdfDocument doc = getDocument();
        if (doc != null)
            return doc.getWriter();
        return null;
    }

    /**
     * Creates new instance of object.
     *
     * @return new instance of object.
     */
    abstract protected <T extends PdfObject> T newInstance();

    /**
     * Copies object content from object 'from'.
     *
     * @param from     object to copy content from.
     * @param document document to copy object to.
     */
    abstract protected void copyContent(PdfObject from, PdfDocument document) throws PdfException;


}