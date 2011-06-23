package frontlinesms2

class Contact {
	String name
	String address
    String notes

	static hasMany = [groups: Group]
	static belongsTo = Group 

    static constraints = {
		name(blank: true, maxSize: 255, validator: { val, obj ->
				if(val == '') {
					obj.address != ''
					obj.address != null
				}
		})
		address(unique: true, nullable: true, validator: { val, obj ->
				if(val == '') {
					obj.name != ''
					obj.name != null
				}
		})
        notes(nullable: true, maxSize: 1024)
	}

	boolean isMemberOf(Group group) {
	   groups.contains(group)
	}

	def getInboundMessagesCount() {
		address? Fmessage.countByDst(address): 0
	}

	def getOutboundMessagesCount() {
		address? Fmessage.countBySrc(address): 0
	}
}
