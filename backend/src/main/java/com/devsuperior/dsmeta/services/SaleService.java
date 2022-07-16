package com.devsuperior.dsmeta.services;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.devsuperior.dsmeta.entities.Sale;
import com.devsuperior.dsmeta.repositories.SaleRepository;

@Service
public class SaleService {

	@Autowired
	private SaleRepository repository;
	
	@Autowired
	private SmsService smsService;
	
	private static final DecimalFormatSymbols REAL = new DecimalFormatSymbols(new Locale("pt","BR"));
	
	public Page<Sale> findSales(String minDate, String maxDate, Pageable pageable) {
		
		LocalDate today = LocalDate.ofInstant(Instant.now(), ZoneId.systemDefault());
		
		LocalDate min = !StringUtils.hasText(minDate) ? today.minusDays(365) : LocalDate.parse(minDate);
		LocalDate max = !StringUtils.hasText(maxDate) ? today : LocalDate.parse(maxDate);
		
		return repository.findSales(min, max, pageable);
	}
	
	public void notifySale(Long saleId) {
		Sale sale = repository.findById(saleId)
				.orElseThrow(() -> new RuntimeException("Venda não encontrada"));
		
		String date = sale.getDate().getMonthValue() + "/" + sale.getDate().getYear();
		String amount = new DecimalFormat("###,###,##0.00", REAL).format(sale.getAmount());
		
		String msg = new StringBuilder()
			.append("O vendedor " + sale.getSellerName())
			.append(" foi destaque em " + date)
			.append(" com um total de R$ " + amount)
			.toString();
		
		smsService.sendSms(msg);
	}
}
