import os
from fpdf import FPDF

pdf = FPDF()
for i in range(1, 8):
    pdf.add_page()
    pdf.set_font("Helvetica", size=16)
    pdf.cell(200, 10, txt=f"Chapter {i}: Research Paper Overview", ln=1, align="L")
    pdf.set_font("Helvetica", size=12)
    pdf.cell(200, 10, txt=f"This is page {i} of the comprehensive research document.", ln=1, align="L")
    pdf.cell(200, 10, txt="Continuando con el tablero creado y las descripciones con el objetivo de la tarea.", ln=1, align="L")
    pdf.cell(200, 10, txt="Mathematical representations and analysis of systems in high performance.", ln=1, align="L")

pdf.output("sample_paper.pdf")
print("PDF created")
