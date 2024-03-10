package com.taxah.diplom_client.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taxah.diplom_client.model.calculate.Debt;
import com.taxah.diplom_client.model.dataBase.*;
import com.taxah.diplom_client.model.dataBase.dto.PayFactDTO;
import com.taxah.diplom_client.model.dataBase.dto.ProductUsingDTO;
import com.taxah.diplom_client.service.ClientService;
import com.taxah.diplom_client.service.feign.CalculateFeign;
import com.taxah.diplom_client.service.feign.DataBaseFeign;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Controller
@RequestMapping("/client")
public class ClientController {
    private final ObjectMapper objectMapper;
    private final ClientService service;
    private final DataBaseFeign apiDbService;
    private final CalculateFeign apiCalculateService;

    @GetMapping
    public String home() {
        return "home";
    }

    @GetMapping("/session/{id}")
    public String getSession(@PathVariable Long id, Model model) {
        Session mySession = apiDbService.getSession(id);
        model.addAttribute("mySession", mySession);
        return "model";
    }

    @PostMapping("/session/find")
    public String findSession(@RequestParam("id") Long id) {
        if (id == null) {
            return "redirect:/error";
        }
        return "redirect:/client/session/" + id;
    }

//    @GetMapping("/session/create")
//    public String createSession(@RequestBody List<TempUser> users) {
//        Long id = null;
//        if (users.get(0) != null) {
//            id = users.get(0).getId();
//        }
//        apiService.createNewSession(users, id);
//        return "home";
//    }

    @PostMapping("session/check")
    public String checkCreate(@RequestParam("checkName") String checkName,
                              @RequestParam("sessionId") Long sessionId){
        apiDbService.createCheck(checkName,sessionId);
        return "redirect:/client/session/" + sessionId;
    }

    @PostMapping("/session/add/temp_user")
    public String addMember(TempUser tempUser) {
        System.out.println(apiDbService.addGuestMember(tempUser));
        return "redirect:/client/session/" + tempUser.getSessionId();
    }

    @DeleteMapping("/session/member/{id}")
    public String deleteMember(@PathVariable Long id) {
        Long sessionId = apiDbService.deleteMember(id);
        return "redirect:/client/session/" + sessionId;
    }

    @DeleteMapping("/session/check/{id}")
    public String deleteCheck(@PathVariable Long id){
        Long sessionId = apiDbService.deleteCheck(id);
        return "redirect:/client/session/" + sessionId;
    }

    @DeleteMapping("/session/payfact/{id}")
    public String deletePayFact(@PathVariable Long id) {
        Long sessionId = apiDbService.deletePayFact(id);
        return "redirect:/client/session/" + sessionId;
    }

    @PostMapping("/session/payfact")
    public String payFactFormAdd(@RequestParam("sessionId") Long sessionId,
                                 @RequestParam("checkId") Long checkId,
                                 Model model) {
        Session mySession = apiDbService.getSession(sessionId);
        model.addAttribute("mySession", mySession);
        model.addAttribute("checkId", checkId);
        return "/payFact/addPayFact";
    }

    @PostMapping("/session/add/payfact")
    public String addPayFact(@ModelAttribute PayFactDTO payFactDTO, @RequestParam("sessionId") Long sessionId) {
        apiDbService.addPayFact(payFactDTO);
        return "redirect:/client/session/" + sessionId;
    }

    @PostMapping("/session/update/payfact")
    public String payFactFormUpdate(@RequestParam("sessionId") Long sessionId,
                                    @RequestParam("payFactId") Long payFactId,
                                    @RequestParam("checkId") Long checkId, Model model) {
        Session session = apiDbService.getSession(sessionId);
        PayFact payFact = apiDbService.getPayFact(payFactId);
        payFact.setCheckId(checkId);
        model.addAttribute("mySession", session);
        model.addAttribute("payFact", payFact);
        return "/payFact/updatePayFact";
    }

    @PutMapping("/session/update/payfact/up")
    public String updatePayFact(@ModelAttribute("payFact") PayFact payFact,
                                @RequestParam("sessionId") Long sessionId,
                                @RequestParam("tempUserid") Long tempUserid) {
        TempUser tempUser = apiDbService.getTempUser(tempUserid);
        payFact.setTempUser(tempUser);
        apiDbService.updatePayFact(payFact);
        return "redirect:/client/session/" + sessionId;
    }

    @PostMapping("/calc/execute")
    public String calculateSession(@RequestParam("sessionId") Long sessionId, Model model){
        Session session = apiDbService.getSession(sessionId);
        List<Debt> debtList = apiCalculateService.calculate(session);
        System.out.println(debtList);
        model.addAttribute("mySession", session);
        model.addAttribute("debtList", debtList);
        return "result";
    }

    @PostMapping("/product/create")
    public String productCreateButton(@RequestParam("checkId") Long checkId,
                                      @RequestParam("sessionId") Long sessionId,
                                      Model model){
        Session session = apiDbService.getSession(sessionId);
        List<TempUser> members = session.getMembersList();
        ProductUsing productUsing = new ProductUsing();
        productUsing.setCheckId(checkId);
        model.addAttribute("productUsing",productUsing);
        model.addAttribute("checkId", checkId);
        model.addAttribute("members",members);
        model.addAttribute("sessionId",sessionId);
        return "/productUsing/addProductUsing";
    }
    @PostMapping("/addProduct")
    public String productCreate(@ModelAttribute ProductUsing productUsing,
                                @RequestParam("sessionId") Long sessionId){
        ProductUsingDTO pDTO = new ProductUsingDTO();
        pDTO.setCheckId(productUsing.getCheckId());
        pDTO.setProductName(productUsing.getProductName());
        pDTO.setCost(productUsing.getCost());
        pDTO.setTempUsers(new ArrayList<TempUser>());
        ProductUsing newProductUsing = apiDbService.addProductUsing(pDTO);
        System.out.println(newProductUsing);
        return "redirect:/client/session/"+sessionId;
    }
    @PostMapping("/addProduct/addUser")
    public String addTempUserToProduct(@RequestParam("userId") Long userId,
                                       @RequestParam("sessionId") Long sessionId,
                                       @RequestParam("productUsingId") Long productUsingId){
        System.out.println(userId);
        TempUser tempUser = apiDbService.getTempUser(userId);
        apiDbService.addTempUserToProduct(productUsingId,tempUser);
        return "redirect:/client/session/"+sessionId;
    }



}
