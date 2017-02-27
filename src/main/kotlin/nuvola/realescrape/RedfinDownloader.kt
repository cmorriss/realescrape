package nuvola.realescrape

import org.openqa.selenium.WebElement
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.firefox.internal.ProfilesIni
import org.openqa.selenium.support.ui.Select
import org.openqa.selenium.support.ui.WebDriverWait


fun main(args: Array<String>) {
    val download_path = "/Volumes/Code/realescrape/csv_data/"

    System.setProperty("webdriver.gecko.driver", "/Users/sdechris/ws/realescrape/lib/geckodriver")

    val profiles = ProfilesIni()
    val profile = profiles.getProfile("redfin.selenium")
    profile.setPreference("browser.download.folderList", 2)
    profile.setPreference("browser.download.manager.showWhenStarting", false)
    profile.setPreference("browser.download.dir", download_path)
    profile.setPreference("browser.helperApps.neverAsk.saveToDisk", "text/csv")
    var oldHtmlId: WebElement? = null

    val driver = FirefoxDriver(profile)
    try {
        ZipCodes.values.forEach {
            driver.navigate().to("https://www.redfin.com")
            val driverWait = WebDriverWait(driver, 30, 400)
            if (oldHtmlId != null) {
                driverWait.until { oldHtmlId != driver.findElementByTagName("html") }
            }
            oldHtmlId = driver.findElementByTagName("html")

            val searchBox = driverWait.until { driver.findElementById("search-box-input") }
            Thread.sleep(1000)
            searchBox.clear()
            Thread.sleep(1000)
            searchBox.sendKeys(it.key)
            Thread.sleep(1000)
            searchBox.submit()

            var element = driverWait.until {
                val homesSummary = driver.findElementByClassName("homes")
                println("homesSummary = $homesSummary, text = ${homesSummary.text}")
                if (homesSummary.text.trim() == "Showing 0 Homes") {
                    homesSummary
                } else {
                    driver.findElementById("download-and-save")
                }
            }
            if ("Showing 0 Homes" != element.text) {
                driverWait.until { driver.findElementByXPath("//button[@data-rf-test-id='filterButton']") }.click()
                driverWait.until { driver.findElementByXPath("//input[@name='uipt2']") }.click()
                "//input[@name='uipt3']".click(driver)
                "//input[@name='uipt4']".click(driver)
                "//input[@name='uipt5']".click(driver)
                "//input[@name='uipt6']".click(driver)
                "//input[@name='soldsCheck']".click(driver)
                "//input[@name='forSaleCheck']".click(driver)
                "//input[@name='fsbo']".click(driver)

                val select = Select(driver.findElementByXPath("//select[@name='solds']"))
                select.selectByVisibleText("Last 6 months")
            } else {
                println("Found 0 homes for zip code ${it.key}")
            }
            Thread.sleep(100000)
        }
    } finally {
        //driver.quit()
    }
}

fun String.click(driver: FirefoxDriver) {
    driver.findElementByXPath(this).click()
}