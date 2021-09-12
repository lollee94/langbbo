package com.example.langbbo

import java.text.DecimalFormat

class EnglishNumberToWords { // app/src/main에 있음

    companion object{
        private val tensNames = arrayOf(
                "",
                " ten",
                " twenty",
                " thirty",
                " forty",
                " fifty",
                " sixty",
                " seventy",
                " eighty",
                " ninety"
        )

        private val numNames = arrayOf(
                "",
                " one",
                " two",
                " three",
                " four",
                " five",
                " six",
                " seven",
                " eight",
                " nine",
                " ten",
                " eleven",
                " twelve",
                " thirteen",
                " fourteen",
                " fifteen",
                " sixteen",
                " seventeen",
                " eighteen",
                " nineteen"
        )

        private fun convertLessThanOneThousand(number: Int): String {
            var number = number
            var soFar: String

            if (number % 100 < 20) {
                soFar = numNames[number % 100]
                number /= 100
            } else {
                soFar = numNames[number % 10]
                number /= 10

                soFar = tensNames[number % 10] + soFar
                number /= 10
            }
            return if (number == 0) soFar else numNames[number] + " hundred" + soFar
        }

        fun convert(number: Long): String {
            // 0 to 999 999 999 999
            if (number == 0L) {
                return "zero"
            }

            // pad with "0"
            val mask = "000000000000"
            val df = DecimalFormat(mask)
            var snumber = df.format(number)

            // XXXnnnnnnnnn
            val billions = Integer.parseInt(snumber.substring(0, 3))
            // nnnXXXnnnnnn
            val millions = Integer.parseInt(snumber.substring(3, 6))
            // nnnnnnXXXnnn
            val hundredThousands = Integer.parseInt(snumber.substring(6, 9))
            // nnnnnnnnnXXX
            val thousands = Integer.parseInt(snumber.substring(9, 12))

            val tradBillions: String
            when (billions) {
                0 -> tradBillions = ""
                1 -> tradBillions = convertLessThanOneThousand(billions) + " billion "
                else -> tradBillions = convertLessThanOneThousand(billions) + " billion "
            }
            var result = tradBillions

            val tradMillions: String
            when (millions) {
                0 -> tradMillions = ""
                1 -> tradMillions = convertLessThanOneThousand(millions) + " million "
                else -> tradMillions = convertLessThanOneThousand(millions) + " million "
            }
            result += tradMillions

            val tradHundredThousands: String
            when (hundredThousands) {
                0 -> tradHundredThousands = ""
                1 -> tradHundredThousands = "one thousand "
                else -> tradHundredThousands =
                        convertLessThanOneThousand(hundredThousands) + " thousand "
            }
            result = result + tradHundredThousands

            val tradThousand: String
            tradThousand = convertLessThanOneThousand(thousands)
            result = result + tradThousand

            // remove extra spaces!
            return result.replace("^\\s+".toRegex(), "").replace("\\b\\s{2,}\\b".toRegex(), " ")
        }

    }
}