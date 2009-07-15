/*
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.broadleafcommerce.order.web;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.broadleafcommerce.catalog.service.CatalogService;
import org.broadleafcommerce.order.dao.FulfillmentGroupItemDao;
import org.broadleafcommerce.order.dao.OrderDao;
import org.broadleafcommerce.order.dao.OrderItemDao;
import org.broadleafcommerce.order.domain.FulfillmentGroup;
import org.broadleafcommerce.order.domain.Order;
import org.broadleafcommerce.order.service.CartService;
import org.broadleafcommerce.order.service.FulfillmentGroupService;
import org.broadleafcommerce.order.service.type.OrderStatus;
import org.broadleafcommerce.order.web.model.FindOrderForm;
import org.broadleafcommerce.payment.service.PaymentInfoService;
import org.broadleafcommerce.pricing.dao.ShippingRateDao;
import org.broadleafcommerce.pricing.service.exception.PricingException;
import org.broadleafcommerce.profile.dao.AddressDao;
import org.broadleafcommerce.profile.dao.CustomerDao;
import org.broadleafcommerce.profile.dao.StateDao;
import org.broadleafcommerce.profile.service.CustomerService;
import org.broadleafcommerce.profile.web.CustomerState;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller("viewOrderController")
public class ViewOrderController {

    @Resource(name="blCustomerState")
    private CustomerState customerState;
    @Resource(name="blCartService")
    protected CartService cartService;
    @Resource(name="blCustomerService")
    protected CustomerService customerService;
    @Resource(name="blOrderDao")
    protected OrderDao orderDao;
    @Resource(name="blShippingRatesDao")
    protected ShippingRateDao shippingRateDao;
    @Resource(name="blFulfillmentGroupService")
    protected FulfillmentGroupService fulfillmentGroupService;
    @Resource(name="blPaymentInfoService")
    protected PaymentInfoService paymentInfoService;
    @Resource(name="blCatalogService")
    protected CatalogService catalogService;
    @Resource(name="blAddressDao")
    protected AddressDao addressDao;
    @Resource(name="blStateDao")
    protected StateDao stateDao;
    @Resource(name="blCustomerDao")
    protected CustomerDao customerDao;
    @Resource(name="blFulfillmentGroupItemDao")
    protected FulfillmentGroupItemDao fulfillmentGroupItemDao;
    @Resource(name="blOrderItemDao")
    protected OrderItemDao orderItemDao;

    @RequestMapping(method =  {RequestMethod.GET})
    public String viewOrders (ModelMap model, HttpServletRequest request) throws PricingException {
        List<Order> orders = cartService.findOrdersForCustomer(customerState.getCustomer(request), OrderStatus.SUBMITTED);
        model.addAttribute("orderList", orders);
        return "listOrders";
    }

    @RequestMapping(method = {RequestMethod.GET})
    public String viewOrderDetails (ModelMap model, HttpServletRequest request, @RequestParam(required = true) String orderNumber) {
        Order order = cartService.findOrderByOrderNumber(orderNumber);
        if (order == null) {
            return "findOrderError";
        }

        model.addAttribute("order", order);
        return "viewOrderDetails";
    }

    @RequestMapping(method =  {RequestMethod.GET})
    public String findOrder (ModelMap model, HttpServletRequest request) {
        model.addAttribute("findOrderForm", new FindOrderForm());
        return "findOrder";
    }

    @RequestMapping(method =  {RequestMethod.POST})
    public String processFindOrder (@ModelAttribute FindOrderForm findOrderForm, ModelMap model, HttpServletRequest request) {
        boolean zipFound = false;
        Order order = cartService.findOrderByOrderNumber(findOrderForm.getOrderNumber());

        if (order == null) {
            return "findOrderError";
        }

        List<FulfillmentGroup> orderFulfillmentGroups = order.getFulfillmentGroups();
        if (orderFulfillmentGroups != null ) {
            orderLoop: for (FulfillmentGroup fulfillmentGroup : orderFulfillmentGroups)  {
                if (fulfillmentGroup.getAddress().getPostalCode().equals(findOrderForm.getPostalCode())) {
                    zipFound = true;
                    break orderLoop;
                }
            }
        }

        if (zipFound) {
            return viewOrderDetails(model, request, order.getOrderNumber());
        }

        return "findOrderError";
    }
}
