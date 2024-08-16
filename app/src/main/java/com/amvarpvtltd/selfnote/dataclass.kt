import java.util.UUID


var myGlobalMobileDeviceId: String = ""


data class dataclass(
    val title: String = "",
    val description: String = "",
    var id: String = UUID.randomUUID().toString(),
    var mymobiledeviceid: String =  myGlobalMobileDeviceId
)
