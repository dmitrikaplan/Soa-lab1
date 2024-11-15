package ru.kaplaan.productservice

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import kotlin.test.assertFailsWith
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import ru.kaplaan.productservice.domain.entity.Coordinates
import ru.kaplaan.productservice.domain.entity.Product
import ru.kaplaan.productservice.domain.exception.not_found.ProductNotFoundException
import ru.kaplaan.productservice.repository.ProductRepository
import ru.kaplaan.productservice.service.impl.ProductServiceImpl
import ru.kaplaan.productservice.web.dto.CoordinatesDto
import ru.kaplaan.productservice.web.dto.ProductDto
import ru.kaplaan.productservice.web.dto.UnitOfMeasure

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = [
        TestConfig::class
    ],
)
class ProductServiceIT {

    @Autowired
    lateinit var mvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var productRepository: ProductRepository

    @BeforeEach
    fun beforeEach(){
        val products = listOf(
            Product(
                name = "Menstrual pads",
                coordinates = Coordinates(x = 10, y = 20),
                price = 100,
                manufactureCost = 100.0f,
                unitOfMeasure = UnitOfMeasure.UNITS,
                owner = null,
            ),
            Product(
                name = "Menstrual cups",
                coordinates = Coordinates(x = 10, y = 20),
                price = 250,
                manufactureCost = 100.0f,
                unitOfMeasure = UnitOfMeasure.UNITS,
                owner = null,
            ),
            Product(
                name = "Ibuprofen",
                coordinates = Coordinates(x = 10, y = 20),
                price = 1337,
                manufactureCost = 100.0f,
                unitOfMeasure = UnitOfMeasure.MILLIGRAMS,
                owner = null,
            ),
            Product(
                name = "Noshpa",
                coordinates = Coordinates(x = 10, y = 20),
                price = 4848,
                manufactureCost = 500.0f,
                unitOfMeasure = UnitOfMeasure.MILLIGRAMS,
                owner = null,
            ),
            Product(
                name = "Candles",
                coordinates = Coordinates(x = 10, y = 20),
                price = 4848,
                manufactureCost = 500.0f,
                unitOfMeasure = UnitOfMeasure.UNITS,
                owner = null,
            ),
        )
        productRepository.saveAll(products)
    }

    @AfterEach
    fun afterEach() {
        productRepository.clear()
    }

    @Test
    fun `test lookup by id`() {
      val products = productRepository.findAll();
      assert(products.size > 0);
      val target1 = products.first();
      val target2 = products.last();

      val service = ProductServiceImpl(productRepository)
      val retrieved1 = service.getById(target1.id)
      val retrieved2 = service.getById(target2.id)

      assertEquals(target1, retrieved1)
      assertEquals(target2, retrieved2)
    }

    @Test
    fun `test lookup failure by id`() {
      val service = ProductServiceImpl(productRepository)
      
      assertFailsWith<ProductNotFoundException>{
        service.getById(-1)
      }
    }

    @Test
    fun `save valid product`() {
        val product = ProductDto(
            name = "productName",
            coordinates = CoordinatesDto(x = 10, y = 20),
            price = 100,
            manufactureCost = 100.0f,
            unitOfMeasure = UnitOfMeasure.KILOGRAMS,
            owner = null
        )
        val productFromServer = saveProduct(product)
        assertEquals(product, productFromServer)
    }

    fun saveProduct(productDto: ProductDto): ProductDto {
       return mvc.post("/products") {
            content = objectMapper.writeValueAsString(productDto)
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status {
                isOk()
            }
        }.andReturn()
            .response
            .contentAsString
            .let {
                objectMapper.readValue(it, ProductDto::class.java)
            }
    }


    fun getAllProducts(): List<ProductDto>? {
        return mvc.get("products/1")
            .andExpect {
                status {
                    isOk()
                }
            }.andReturn()
            .response
            .contentAsString
            .let {
                toParametrizedList(it)
            }
    }


    fun toParametrizedList(json: String): List<ProductDto>? {
        return objectMapper.readValue(
            json,
            object: TypeReference<List<ProductDto>>() {}
            )
    }
}