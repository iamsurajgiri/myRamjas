package delhi.university.ramjas.college.parcels

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SetupParcel(
    var name: String? = null,
    var phone: String? = null,
    var deviceId: String? = null,
    var image: String? = null,
    var verification: String? = null,
    var collegeID: String? = null,
    var idType: String? = null,
    var homestate: String? = null,
    var department: String? = null,
    var semester: String? = null,
    var hobbies: List<String>? = null,
    var careers: List<String>? = null,
    var societies: List<String>? = null,
    var socials: Map<String,String>? = null
) : Parcelable
