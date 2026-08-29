package com.example.services

data class LocationNode(
    val stateId: String,
    val stateName: String,
    val districts: List<DistrictNode>
)

data class DistrictNode(
    val districtId: String,
    val districtName: String,
    val mandals: List<MandalNode>
)

data class MandalNode(
    val mandalId: String,
    val mandalName: String,
    val pincodes: List<String>,
    val lat: Double,
    val lng: Double,
    val locality: String
)

object LocationHierarchyService {
    val states = listOf(
        LocationNode(
            stateId = "TG",
            stateName = "Telangana",
            districts = listOf(
                DistrictNode(
                    districtId = "TG_RR",
                    districtName = "Rangareddy",
                    mandals = listOf(
                        MandalNode("TG_RR_SH", "Shamshabad", listOf("501218", "509325"), 17.2543, 78.4356, "Shamshabad Airport Area"),
                        MandalNode("TG_RR_IB", "Ibrahimpatnam", listOf("501506"), 17.1945, 78.6445, "Ibrahimpatnam Town"),
                        MandalNode("TG_RR_RN", "Rajendranagar", listOf("500030", "500048"), 17.3298, 78.4012, "Rajendranagar Mandal"),
                        MandalNode("TG_RR_NK", "Narkhuda", listOf("501504"), 17.2112, 78.3892, "Narkhuda Village")
                    )
                ),
                DistrictNode(
                    districtId = "TG_HYD",
                    districtName = "Hyderabad",
                    mandals = listOf(
                        MandalNode("TG_HYD_AM", "Ameerpet", listOf("500016"), 17.4375, 78.4482, "Ameerpet Circle"),
                        MandalNode("TG_HYD_BN", "Banjara Hills", listOf("500034"), 17.4156, 78.4356, "Banjara Hills Road No 12"),
                        MandalNode("TG_HYD_SEC", "Secunderabad", listOf("500003"), 17.4399, 78.4983, "Secunderabad Railway Station"),
                        MandalNode("TG_HYD_CHAR", "Charminar", listOf("500002"), 17.3616, 78.4747, "Old City Charminar")
                    )
                ),
                DistrictNode(
                    districtId = "TG_MED",
                    districtName = "Medchal-Malkajgiri",
                    mandals = listOf(
                        MandalNode("TG_MED_KUK", "Kukatpally", listOf("500072"), 17.4947, 78.3996, "Kukatpally Housing Board"),
                        MandalNode("TG_MED_ALW", "Alwal", listOf("500010"), 17.5023, 78.5234, "Old Alwal"),
                        MandalNode("TG_MED_QUTH", "Quthbullapur", listOf("500055"), 17.5089, 78.4612, "Gajularamaram Area")
                    )
                ),
                DistrictNode(
                    districtId = "TG_SAN",
                    districtName = "Sangareddy",
                    mandals = listOf(
                        MandalNode("TG_SAN_PAT", "Patancheru", listOf("502319"), 17.5288, 78.2676, "Patancheru Industrial Area"),
                        MandalNode("TG_SAN_ZAH", "Zaheerabad", listOf("502220"), 17.6788, 77.6074, "Zaheerabad Town")
                    )
                )
            )
        ),
        LocationNode(
            stateId = "MH",
            stateName = "Maharashtra",
            districts = listOf(
                DistrictNode(
                    districtId = "MH_MUM",
                    districtName = "Mumbai City",
                    mandals = listOf(
                        MandalNode("MH_MUM_COL", "Colaba", listOf("400005"), 18.9067, 72.8147, "Colaba Causeway"),
                        MandalNode("MH_MUM_AND", "Andheri", listOf("400053"), 19.1136, 72.8697, "Andheri West"),
                        MandalNode("MH_MUM_DAD", "Dadar", listOf("400028"), 19.0178, 72.8478, "Dadar TT Circle")
                    )
                ),
                DistrictNode(
                    districtId = "MH_PUN",
                    districtName = "Pune",
                    mandals = listOf(
                        MandalNode("MH_PUN_SHI", "Shivajinagar", listOf("411005"), 18.5314, 73.8446, "Shivajinagar Court"),
                        MandalNode("MH_PUN_KOT", "Kothrud", listOf("411038"), 18.5074, 73.8077, "Kothrud Depot"),
                        MandalNode("MH_PUN_KHED", "Khed Shivapur", listOf("412205"), 18.3498, 73.8567, "Khed Shivapur Highway Toll")
                    )
                ),
                DistrictNode(
                    districtId = "MH_NAG",
                    districtName = "Nagpur",
                    mandals = listOf(
                        MandalNode("MH_NAG_SAD", "Sadar", listOf("440001"), 21.1611, 79.0831, "Sadar Bazar Nagpur")
                    )
                )
            )
        ),
        LocationNode(
            stateId = "KA",
            stateName = "Karnataka",
            districts = listOf(
                DistrictNode(
                    districtId = "KA_BLR",
                    districtName = "Bangalore Urban",
                    mandals = listOf(
                        MandalNode("KA_BLR_BLR", "Bangalore North", listOf("560001"), 12.9716, 77.5946, "MG Road Bangalore"),
                        MandalNode("KA_BLR_KOR", "Koramangala", listOf("560034"), 12.9352, 77.6245, "Koramangala 4th Block"),
                        MandalNode("KA_BLR_WHT", "Whitefield", listOf("560066"), 12.9698, 77.7500, "Whitefield ITPB")
                    )
                ),
                DistrictNode(
                    districtId = "KA_MYS",
                    districtName = "Mysore",
                    mandals = listOf(
                        MandalNode("KA_MYS_VV", "Vidyaranyapuram", listOf("570008"), 12.2856, 76.6432, "Mysore Palace South")
                    )
                )
            )
        ),
        LocationNode(
            stateId = "TN",
            stateName = "Tamil Nadu",
            districts = listOf(
                DistrictNode(
                    districtId = "TN_CHN",
                    districtName = "Chennai",
                    mandals = listOf(
                        MandalNode("TN_CHN_MYL", "Mylapore", listOf("600004"), 13.0339, 80.2707, "Mylapore Tank"),
                        MandalNode("TN_CHN_TGR", "T. Nagar", listOf("600017"), 13.0401, 80.2337, "Panagal Park")
                    )
                ),
                DistrictNode(
                    districtId = "TN_CBE",
                    districtName = "Coimbatore",
                    mandals = listOf(
                        MandalNode("TN_CBE_RSM", "RS Puram", listOf("641002"), 11.0168, 76.9558, "RS Puram West"),
                        MandalNode("TN_CBE_SIT", "Singanallur", listOf("641005"), 10.9985, 77.0321, "Singanallur Bus Stand")
                    )
                )
            )
        ),
        LocationNode(
            stateId = "AP",
            stateName = "Andhra Pradesh",
            districts = listOf(
                DistrictNode(
                    districtId = "AP_VSP",
                    districtName = "Visakhapatnam",
                    mandals = listOf(
                        MandalNode("AP_VSP_DWK", "Dwaraka Nagar", listOf("530016"), 17.7292, 83.3093, "Dwaraka Nagar Vizag"),
                        MandalNode("AP_VSP_GAJ", "Gajuwaka", listOf("530026"), 17.6901, 83.2098, "Gajuwaka Junction")
                    )
                ),
                DistrictNode(
                    districtId = "AP_VIJ",
                    districtName = "NTR Vijayawada",
                    mandals = listOf(
                        MandalNode("AP_VIJ_BZ", "Bezant Road", listOf("520002"), 16.5062, 80.6480, "Vijayawada City Centre")
                    )
                )
            )
        ),
        LocationNode(
            stateId = "GJ",
            stateName = "Gujarat",
            districts = listOf(
                DistrictNode(
                    districtId = "GJ_AMD",
                    districtName = "Ahmedabad",
                    mandals = listOf(
                        MandalNode("GJ_AMD_NAV", "Navrangpura", listOf("380009"), 23.0368, 72.5612, "CG Road Ahmedabad")
                    )
                )
            )
        ),
        LocationNode(
            stateId = "UP",
            stateName = "Uttar Pradesh",
            districts = listOf(
                DistrictNode(
                    districtId = "UP_LKO",
                    districtName = "Lucknow",
                    mandals = listOf(
                        MandalNode("UP_LKO_HAZ", "Hazratganj", listOf("226001"), 26.8467, 80.9462, "Hazratganj Market")
                    )
                )
            )
        ),
        LocationNode(
            stateId = "WB",
            stateName = "West Bengal",
            districts = listOf(
                DistrictNode(
                    districtId = "WB_KOL",
                    districtName = "Kolkata",
                    mandals = listOf(
                        MandalNode("WB_KOL_PARK", "Park Street", listOf("700016"), 22.5539, 88.3524, "Park Street Commercial Area")
                    )
                )
            )
        ),
        LocationNode(
            stateId = "TR",
            stateName = "Tripura",
            districts = listOf(
                DistrictNode(
                    districtId = "TR_WST",
                    districtName = "West Tripura",
                    mandals = listOf(
                        MandalNode("TR_WST_AGR", "Agartala Sadar", listOf("799001"), 23.8315, 91.2868, "Agartala City Centre")
                    )
                )
            )
        ),
        LocationNode(
            stateId = "DL",
            stateName = "Delhi NCR",
            districts = listOf(
                DistrictNode(
                    districtId = "DL_NEW",
                    districtName = "New Delhi",
                    mandals = listOf(
                        MandalNode("DL_NEW_CP", "Connaught Place", listOf("110001"), 28.6315, 77.2167, "Inner Circle CP")
                    )
                )
            )
        )
    )

    fun searchStates(query: String): List<LocationNode> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return states
        
        val exactMatches = mutableListOf<LocationNode>()
        val prefixMatches = mutableListOf<LocationNode>()
        val partialMatches = mutableListOf<LocationNode>()

        for (state in states) {
            val name = state.stateName.lowercase()
            when {
                name == q -> exactMatches.add(state)
                name.startsWith(q) -> prefixMatches.add(state)
                name.contains(q) -> partialMatches.add(state)
            }
        }
        return exactMatches + prefixMatches + partialMatches
    }

    fun searchDistricts(stateId: String, query: String): List<DistrictNode> {
        val state = states.find { it.stateId == stateId } ?: return emptyList()
        val q = query.trim().lowercase()
        if (q.isEmpty()) return state.districts

        val exact = mutableListOf<DistrictNode>()
        val prefix = mutableListOf<DistrictNode>()
        val partial = mutableListOf<DistrictNode>()

        for (dist in state.districts) {
            val name = dist.districtName.lowercase()
            when {
                name == q -> exact.add(dist)
                name.startsWith(q) -> prefix.add(dist)
                name.contains(q) -> partial.add(dist)
            }
        }
        return exact + prefix + partial
    }

    fun searchMandals(stateId: String, districtId: String, query: String): List<MandalNode> {
        val state = states.find { it.stateId == stateId } ?: return emptyList()
        val district = state.districts.find { it.districtId == districtId } ?: return emptyList()
        val q = query.trim().lowercase()
        if (q.isEmpty()) return district.mandals

        val exact = mutableListOf<MandalNode>()
        val prefix = mutableListOf<MandalNode>()
        val partial = mutableListOf<MandalNode>()

        for (mandal in district.mandals) {
            val name = mandal.mandalName.lowercase()
            when {
                name == q -> exact.add(mandal)
                name.startsWith(q) -> prefix.add(mandal)
                name.contains(q) -> partial.add(mandal)
            }
        }
        return exact + prefix + partial
    }

    fun validateAndResolvePincode(
        stateId: String,
        districtId: String,
        mandalId: String,
        pincode: String
    ): MandalNode? {
        val state = states.find { it.stateId == stateId } ?: return null
        val district = state.districts.find { it.districtId == districtId } ?: return null
        val mandal = district.mandals.find { it.mandalId == mandalId } ?: return null
        if (mandal.pincodes.contains(pincode)) {
            return mandal
        }
        return null
    }

    fun findByPincode(pincode: String): Triple<LocationNode, DistrictNode, MandalNode>? {
        for (state in states) {
            for (district in state.districts) {
                for (mandal in district.mandals) {
                    if (mandal.pincodes.contains(pincode)) {
                        return Triple(state, district, mandal)
                    }
                }
            }
        }
        return null
    }
}
