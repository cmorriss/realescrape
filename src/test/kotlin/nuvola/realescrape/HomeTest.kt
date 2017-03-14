package nuvola.realescrape

import org.junit.Assert
import org.junit.Test

class HomeTest {
    @Test
    fun testHome() {
        val home = Home("221 B Baker:St/test", "London", "11111", "http://test.com")
        Assert.assertEquals("221_B_Baker_St_test", home.getCleanAddress())
    }
}