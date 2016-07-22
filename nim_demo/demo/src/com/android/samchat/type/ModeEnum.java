package com.android.samchat.type;

public enum ModeEnum {
		CUSTOMER_MODE(0),
		SP_MODE(1);

		private int value;

		ModeEnum(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}

		public static ModeEnum typeOfValue(int value) {
			for (ModeEnum e : values()) {
				if (e.getValue() == value) {
					return e;
				}
			}

			return CUSTOMER_MODE;
		}

		public static int valueOfType(ModeEnum type){
			return type.ordinal();
		}
}