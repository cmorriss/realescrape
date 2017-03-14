package nuvola.realescrape

import org.lightcouch.CouchDbClient
import org.openqa.selenium.By
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.firefox.internal.ProfilesIni
import org.openqa.selenium.support.ui.Select
import org.openqa.selenium.support.ui.WebDriverWait
import java.text.SimpleDateFormat
import java.util.Date

class RedfinDownloader {
    companion object {
        const val downloadPath = "/Volumes/Code/realescrape/csv_data/"
        const val homeDataPath = "/Volumes/Code/realescrape/home_data/"
    }

    private val driver: FirefoxDriver
    private val driverWait: WebDriverWait
    private var filterConfigured = false

    init {
        System.setProperty("webdriver.gecko.driver", "/Users/sdechris/ws/realescrape/lib/geckodriver")

        val profiles = ProfilesIni()
        val profile = profiles.getProfile("redfin2.selenium")
        profile.setPreference("browser.download.folderList", 2)
        profile.setPreference("browser.download.manager.showWhenStarting", false)
        profile.setPreference("browser.download.dir", downloadPath)
        profile.setPreference("browser.helperApps.neverAsk.saveToDisk", "text/csv")

        driver = FirefoxDriver(profile)
        driverWait = WebDriverWait(driver, 30, 400)
    }

    fun downloadSoldCSV() {
        try {
            driver.navigate().to("https://www.redfin.com")

            ZipCodes.values.forEach {
                searchForValue(it.key)

                val homesElement = driverWait.until {
                    driver.findElementByClassName("homes")
                }
                configureFilter()

                if (homesElement.text.trim() != "Showing 0 Homes") {
                    val numHomes = homesElement.text.replace(Regex("^[0-9]"), "")
                    println("Downloading data for $numHomes homes in zip code ${it.key}...")
                    driver.findElementById("download-and-save").click()
                } else {
                    println("Could not find any homes for zip code ${it.key}.")
                }
            }
        } finally {
            driver.quit()
        }
    }

    fun downloadPage(home: Home) {
        val downloadFile = home.prepareDataFile()
        if (!downloadFile.exists()) {
            driver.navigate().to(home.redfinUrl)
            driverWait.until {
                driver.findElementByClassName("schools-content")
            }
            driverWait.until {
                driver.findElementByClassName("amenity-group")
            }
            val pageSource = driver.pageSource
            downloadFile.writeText(pageSource)
        }
    }

    fun downloadHomeData(home: Home) {
        driver.navigate().to(home.redfinUrl)
        home.getSales().forEach {
            val nwmlsNum = it
            val salesRecords = driverWait.until {
                println("retrieving for NWMLS #$nwmlsNum")
                val element = driver.findElementsByXPath("//tr[.//text()='NWMLS #$nwmlsNum']")

                println("found NWMLS elements: $element")
                element

            }
            salesRecords.forEach {
                try {
                    val date = it.findElement(By.className("date-col")).text
                    val type = it.findElement(By.className("event")).text
                    var price = it.findElement(By.className("price-col")).text
                    price = price.replace(",", "").replace("$", "")
                    if (price == "—") {
                        price = "0"
                    }
                    home.addPropertyRecord(PropertyRecord(getDate(date), PropertyRecordType.from(type), price.toInt()))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun uploadData(homes: List<Home>) {
        val dbClient = CouchDbClient()
        val responses = dbClient.bulk(homes, true)
        responses.forEach {
            if (it.error != null) {
                println("Found error when uploading home: ${it.error}")
            }
        }
    }

    fun searchForValue(value: String) {
        val searchBox = driverWait.until { driver.findElementById("search-box-input") }
        Thread.sleep(1000)
        searchBox.clear()
        Thread.sleep(1000)
        searchBox.sendKeys(value)
        Thread.sleep(1000)
        searchBox.submit()
        Thread.sleep(3000)
    }

    fun configureFilter() {
        if (!filterConfigured) {
            Thread.sleep(3000)
            waitClick("//button[@data-rf-test-id='filterButton']")
            waitClick("//input[@name='uipt2']")
            click("//input[@name='uipt3']")
            click("//input[@name='uipt4']")
            click("//input[@name='uipt5']")
            click("//input[@name='uipt6']")
            click("//input[@name='soldsCheck']")
            click("//input[@name='forSaleCheck']")
            click("//input[@name='fsbo']")

            val selectSixMonths = "document.getElementsByName('solds')[0].value='180'";
            driver.executeScript(selectSixMonths)
            val selectElement = driver.findElementByXPath("//select[@name='solds']")
            selectElement.click()
            val select = Select(selectElement)
            select.selectByValue("180")

            Thread.sleep(2000)
            click("//button[@data-rf-test-name='submitButton']")
            Thread.sleep(2000)
            filterConfigured = true
        }
    }

    fun click(xpath: String) {
        driver.findElementByXPath(xpath).click()
    }

    fun waitClick(xpath: String) {
        driverWait.until { driver.findElementByXPath(xpath) }.click()
    }

    private fun getDate(dateString: String): Date {
        return SimpleDateFormat("MMM d, yyyy").parse(dateString)
    }
}


fun main(args: Array<String>) {
    val downloader = RedfinDownloader()
    val homes = RedfinData().soldHomesData
//    val dbClient = CouchDbClient()
//    val existing = dbClient.view("_all_docs").includeDocs(true).query(Home::class.java)
    var counter = 0
    val zipCodes = listOf("98028", "98021", "98011", "98177", "98133", "98125")
    val targetHomes = homes.filter { zipCodes.contains(it.zipCode) }.toCollection(HashSet<Home>())

            targetHomes.forEach {
                counter++
                if (counter % 20 == 0) {
                    println("Finished downloading $counter data files...")
                }
                println("downloading from zip code $it")
                downloader.downloadPage(it)
                println("done downloading page for zip code $it")
            }
    }
