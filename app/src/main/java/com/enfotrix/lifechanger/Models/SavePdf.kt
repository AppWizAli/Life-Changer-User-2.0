package com.enfotrix.lifechanger.Models

import com.enfotrix.lifechanger.Constants
import com.enfotrix.lifechanger.SharedPrefManager
import com.itextpdf.text.BaseColor
import com.itextpdf.text.Document
import com.itextpdf.text.Element
import com.itextpdf.text.FontFactory
import com.itextpdf.text.Paragraph
import com.itextpdf.text.Phrase
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Locale

class SavePdf(private val sharedPrefManager: SharedPrefManager, private val constants: Constants) {
    fun generateInvestmentPDF(outputStream: OutputStream, check: String,type: String): Boolean {
        val document = Document()

        try {
            com.itextpdf.text.pdf.PdfWriter.getInstance(document, outputStream)
            document.open()
            val titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18f, BaseColor.BLACK)
            var title: Paragraph
            var investmentList: List<TransactionModel>
            if (check == "Approved" && type=="Invest") {
                title = Paragraph("Investment Approved Details", titleFont)
                investmentList = sharedPrefManager.getInvestmentReqList().filter {
                    it.status.equals(constants.TRANSACTION_STATUS_APPROVED)
                }.sortedByDescending { it.createdAt }
            } else if(check=="Pending" && type=="Invest"){
                title = Paragraph("Investment Pending Details", titleFont)
                investmentList = sharedPrefManager.getInvestmentReqList().filter {
                    it.status.equals(constants.TRANSACTION_STATUS_PENDING)
                }.sortedByDescending { it.createdAt }
            }else if(check=="Approved" && type=="Withdraw"){
                title = Paragraph("Withdraw Approved Details", titleFont)
                investmentList =sharedPrefManager.getWithdrawReqList().filter{ it.status.equals(constants.TRANSACTION_STATUS_APPROVED) }.sortedByDescending { it.createdAt }
            }else{
                title = Paragraph("Withdraw Pending Details", titleFont)
                investmentList = sharedPrefManager.getWithdrawReqList().filter{ it.status.equals(constants.TRANSACTION_STATUS_PENDING)
                }.sortedByDescending { it.createdAt }
            }

            title.alignment = Element.ALIGN_CENTER
            document.add(title)
            document.add(Paragraph("\n"))
            val table = PdfPTable(5)
            table.widthPercentage = 100f
            val headers = arrayOf(
                Paragraph("Request Date", titleFont),
                Paragraph("Old Balance", titleFont),
                Paragraph("Request Balance", titleFont),
                Paragraph("New Balance", titleFont),
                Paragraph("Clear Date", titleFont)
            )
            for (header in headers) {
                val cell = PdfPCell(Phrase(header))
                cell.horizontalAlignment = Element.ALIGN_CENTER
                table.addCell(cell)
            }
            for (item in investmentList) {
                val rdate = SimpleDateFormat(
                    "dd/MM/yy",
                    Locale.getDefault()
                ).format(item.createdAt.toDate())
                val cdate = item.transactionAt?.toDate()
                    ?.let { SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(it) }
                table.addCell(rdate)
                table.addCell(item.previousBalance)
                table.addCell(item.amount)
                table.addCell(item.newBalance)
                table.addCell(cdate)
            }
            document.add(table)
            document.close()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

}