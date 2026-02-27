package com.erp.finance.web;

import com.erp.finance.domain.ARInvoice;
import com.erp.finance.domain.ARInvoiceItem;
import java.util.List;

public class ARInvoiceDTO {
    private ARInvoice invoice;
    private List<ARInvoiceItem> items;

    public ARInvoiceDTO() {}

    public ARInvoiceDTO(ARInvoice invoice, List<ARInvoiceItem> items) {
        this.invoice = invoice;
        this.items = items;
    }

    public ARInvoice getInvoice() {
        return invoice;
    }

    public void setInvoice(ARInvoice invoice) {
        this.invoice = invoice;
    }

    public List<ARInvoiceItem> getItems() {
        return items;
    }

    public void setItems(List<ARInvoiceItem> items) {
        this.items = items;
    }
}
