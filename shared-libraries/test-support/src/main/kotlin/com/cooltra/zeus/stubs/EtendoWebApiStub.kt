package com.cooltra.zeus.stubs

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.urlMatching
import com.github.tomakehurst.wiremock.common.Slf4jNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration

class EtendoWebApiStub(configuration: WireMockConfiguration = WireMockConfiguration.options().dynamicPort()) {
  val server: WireMockServer

  init {
    configuration.notifier(Slf4jNotifier(false))
    server = WireMockServer(configuration)
  }

  fun start(): EtendoWebApiStub {
    server.start()
    defaultMappings()
    return this
  }

  fun stop() {
    server.stop()
  }

  fun reset() {
    server.resetAll()
    defaultMappings()
  }

  private fun defaultMappings() {
  }

  fun on(mappingBuilder: MappingBuilder) = server.addStubMapping(mappingBuilder.build())

  fun stubGetShops() {
    on(
      get(urlMatching("/shop/getShops"))
        .willReturn(
          aResponse().withStatus(200).withBody(
            """
              {
                "code": 0,
                "message": "Success",
                "data": [
                  {
                    "id": "A1D367FF7BC94C6BA2A385F3A364FE0C",
                    "payment_total_discount_percent": 0,
                    "allows_dev_out_hour_flag": false,
                    "preroll": 24,
                    "preroll_horas": 0,
                    "name": "Milano",
                    "street": "Viale Gian Galeazzo 14",
                    "zipcode": "20136",
                    "phone": "(+39) 389 50 66 538",
                    "fax": null,
                    "email": "milano@cooltra.com",
                    "type": 0,
                    "allows_del_out_hour": false,
                    "center_point": [
                      "9.18383000",
                      "45.45257000"
                    ],
                    "images_full_path": {
                      "150": "https://dikkvgyb042xo.cloudfront.net/img/gallery/shops/type/150/cooltra_shop.png",
                      "370": "https://dikkvgyb042xo.cloudfront.net/img/gallery/shops/type/370/cooltra_shop.png"
                    },
                    "shop_images_full_path": {
                      "150": "https://dikkvgyb042xo.cloudfront.net/img/gallery/shops/150/milano_cs.png",
                      "370": "https://dikkvgyb042xo.cloudfront.net/img/gallery/shops/370/milano_cs.png"
                    },
                    "delivery_point_more_info": null,
                    "city_name": "Milan"
                  },
                  {
                    "id": "5C005DB71BF644AD822F994C0EAF92A2",
                    "payment_total_discount_percent": 0,
                    "allows_dev_out_hour_flag": false,
                    "preroll": 48,
                    "preroll_horas": 0,
                    "name": "Isola Garibaldi (Milan)",
                    "street": "Via Carlo Farini, 52 ",
                    "zipcode": "20159",
                    "phone": "(+39) 389 50 66 538 (whatsapp)",
                    "fax": null,
                    "email": "milano@cooltra.com",
                    "type": 3,
                    "allows_del_out_hour": false,
                    "center_point": [
                      "9.18450000",
                      "45.49210000"
                    ],
                    "images_full_path": {
                      "150": "https://dikkvgyb042xo.cloudfront.net/img/gallery/shops/type/150/cooltra_deliverypoint.png",
                      "370": "https://dikkvgyb042xo.cloudfront.net/img/gallery/shops/type/370/cooltra_deliverypoint.png"
                    },
                    "shop_images_full_path": {
                      "150": "https://dikkvgyb042xo.cloudfront.net/img/gallery/shops/150/milano_cs.png",
                      "370": "https://dikkvgyb042xo.cloudfront.net/img/gallery/shops/370/milano_cs.png"
                    },
                    "delivery_point_more_info": {
                      "description": "<p>If you are traveling to Milano, we offer you an alternative and effective mobility solution to make your stay more comfortable and enjoyable: scooter rental at Isola Garibaldi in Milano.<br />It is a scooter delivery and collection point, delivery point, you will not find a Cooltra physical store here. We have created this meeting point to bring your scooter directly here, so that you can start enjoying your stay in Milano from the moment you arrive.</p>\n<p><br />How does a delivery point work?<br />The delivery point is a service that we have designed specifically to meet your needs and to make your experience better. You won't have to go to one of our Cooltra shops in the city, because we will bring the scooter directly to the meeting point. The experience will be the same as picking it up in a store.<br />When you make your reservation online, you will have to choose this delivery point, and on the day and time you indicate to us, a member of our staff will be waiting for you with the contract and the scooter, or scooters, you have booked.</p>\n<p><br />Time and place of delivery<br />From Monday to Friday from 10 to 13 and from 14 to 16.<br />The service is available only for rentals of a minimum of 3 days. Also, remember that our staff waits at the meeting point for 15 minutes*, but if you have unforeseen events or want to change the time, let us know by email milano@cooltra.com or call or write us on whatsapp at +39 3895066538. Remember that, to make the fastest service, you can also write to us to choose the size of the helmet, or helmets, you need (sizes range from XS to XL).</p>\n<p><br />*For delays of more than 30 minutes not communicated in writing at least 2 hours before the appointment, both on delivery and/or on return, a &euro;50 penalty will be charged. After 45 minutes of delay, the Cooltra staff will leave the meeting point and the reservation will be considered charged in full. The Customer will still have the opportunity to go to a Cooltra office to collect and/or return the booked vehicle, during the office's operating hours. Should the Customer request a new collection at home and/or at the delivery point and/or leave the vehicle at the agreed point, Cooltra reserves the right to charge the transport costs in the order of &euro;150 per vehicle.</p>\n<p><br />What does the service include?<br />Don't be without a scooter during your holidays in Milano, and remember that your booking includes:<br />&bull; A helmet<br />&bull; insurance with franchise<br />&bull; unlimited kilometers<br />Choose the funniest and safest way to move. Furthermore, our team will be happy to give you all the advice you want to make your holiday the best ever.<br /></p>\n<p><br />How to get to the Delivery Point?<br />At Isola Garibaldi of Milano we do not have a physical shop, it is a scooter delivery and collection point. A member of our team will be waiting for you on the road at the address mentioned.<br />For any doubt you can always contact us on whatsapp or by mail.</p>",
                      "url_conditions": "https://www.cooltra.com/en/rental-conditions/"
                    },
                    "city_name": "Milan"
                  },
                  {
                    "id": "9BC473668BE048768DC3594D59E944AD",
                    "payment_total_discount_percent": 0,
                    "allows_dev_out_hour_flag": false,
                    "preroll": 0,
                    "preroll_horas": 1,
                    "name": "Oporto",
                    "street": "Rua Moreira de Sá 38 ",
                    "zipcode": "4100-352",
                    "phone": null,
                    "fax": null,
                    "email": null,
                    "type": 0,
                    "allows_del_out_hour": false,
                    "center_point": [
                      "-8.60602000",
                      "41.14600000"
                    ],
                    "images_full_path": {
                      "150": "https://dikkvgyb042xo.cloudfront.net/img/gallery/shops/type/150/cooltra_shop.png",
                      "370": "https://dikkvgyb042xo.cloudfront.net/img/gallery/shops/type/370/cooltra_shop.png"
                    },
                    "shop_images_full_path": {
                      "150": "https://dikkvgyb042xo.cloudfront.net/img/gallery/shops/150/oporto_cs.png",
                      "370": "https://dikkvgyb042xo.cloudfront.net/img/gallery/shops/370/oporto_cs.png"
                    },
                    "delivery_point_more_info": null,
                    "city_name": "Porto"
                  }
                ]
              }
            """.trimIndent(),
          ),
        ),
    )
  }
}
