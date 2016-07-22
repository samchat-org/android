package com.android.samservice;

public enum TypeEnum {
		CELLPHONE(0),
		UNIQUE_ID(1),
		USERNAME(2);

		private int value;

		TypeEnum(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}

		public static TypeEnum typeOfValue(int value) {
			for (TypeEnum e : values()) {
				if (e.getValue() == value) {
					return e;
				}
			}

			return CELLPHONE;
		}

		public static int valueOfType(TypeEnum type){
			return type.ordinal();
		}
}