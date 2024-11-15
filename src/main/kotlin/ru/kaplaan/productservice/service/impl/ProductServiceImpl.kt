package ru.kaplaan.productservice.service.impl

import org.springframework.stereotype.Service
import ru.kaplaan.productservice.domain.entity.Product
import ru.kaplaan.productservice.domain.exception.FieldNotComparableException
import ru.kaplaan.productservice.domain.exception.PageNumberTooLargeException
import ru.kaplaan.productservice.domain.exception.notFound.FieldNotFoundException
import ru.kaplaan.productservice.domain.exception.notFound.ProductNotFoundException
import ru.kaplaan.productservice.domain.filter.ProductFilter
import ru.kaplaan.productservice.repository.ProductRepository
import ru.kaplaan.productservice.service.ProductService
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

const val pageSize = 10

@Service
class ProductServiceImpl(
    private val productRepository: ProductRepository
): ProductService {

    override fun save(product: Product): Product {
        return productRepository.save(product)
    }

    override fun saveAll(products: List<Product>) {
        productRepository.saveAll(products)
    }

    override fun getById(id: Int): Product {
       return productRepository.findById(id)
            ?: throw ProductNotFoundException()
    }

    override fun update(product: Product): Product {
        return productRepository.update(product)
    }

    override fun deleteById(id: Int) {
        return productRepository.deleteById(id)
    }

    override fun getAll(
        pageNumber: Int
    ): List<Product> {

        val sortedProducts = productRepository.findAll().sortedBy { it.id }

        //TODO filter

        val productsPages = sortedProducts.chunked(pageSize)

        if(productsPages.size < pageNumber)
            throw PageNumberTooLargeException()

        return productsPages[pageNumber - 1]
    }
}