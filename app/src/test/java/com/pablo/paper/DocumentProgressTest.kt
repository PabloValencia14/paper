package com.pablo.paper

import com.google.common.truth.Truth.assertThat
import com.pablo.paper.domain.model.Document
import org.junit.Test

class DocumentProgressTest {

    @Test
    fun documentProgress_page138of548_calculates25Percent() {
        val doc = Document(
            id = "doc1",
            uri = "content://sample",
            name = "RLbook2020trimmed.pdf",
            pageCount = 548,
            currentPage = 138,
            progress = 138f / 548f
        )

        // 138 / 548 = 0.2518 -> 25%
        assertThat(doc.progressPercentage).isEqualTo(25)
    }

    @Test
    fun documentProgress_bounds_areClamped() {
        val docStart = Document(
            id = "doc1",
            uri = "content://sample",
            name = "paper.pdf",
            pageCount = 10,
            currentPage = 1,
            progress = 0.1f
        )
        assertThat(docStart.progressPercentage).isEqualTo(10)

        val docComplete = Document(
            id = "doc2",
            uri = "content://sample",
            name = "paper.pdf",
            pageCount = 10,
            currentPage = 10,
            progress = 1.0f
        )
        assertThat(docComplete.progressPercentage).isEqualTo(100)
    }
}
