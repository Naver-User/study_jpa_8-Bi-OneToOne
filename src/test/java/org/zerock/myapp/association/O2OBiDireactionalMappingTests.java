package org.zerock.myapp.association;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.FlushModeType;
import javax.persistence.Persistence;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Timeout;
import org.zerock.myapp.entity.Locker2;
import org.zerock.myapp.entity.Student2;
import org.zerock.myapp.util.PersistenceUnits;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;


//@Log4j2
@Slf4j

@NoArgsConstructor

@TestInstance(Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
public class O2OBiDireactionalMappingTests {
	private EntityManagerFactory emf;
	private EntityManager em;
	
	
	@BeforeAll
	void beforeAll() {	// 1회성 전처리
		log.trace("beforeAll() invoked.");
		
		// -- 1 ------------
//		this.emf = Persistence.createEntityManagerFactory(PersistenceUnits.H2);
//		this.emf = Persistence.createEntityManagerFactory(PersistenceUnits.ORACLE);
		this.emf = Persistence.createEntityManagerFactory(PersistenceUnits.MYSQL);
		
		Objects.requireNonNull(this.emf);

		// -- 2 ------------
		this.em = this.emf.createEntityManager();
		assertNotNull(this.em);
		
		this.em.setFlushMode(FlushModeType.COMMIT);
	} // beforeAll
	
	@AfterAll
	void afterAll() {	// 1회성 전처리
		log.trace("afterAll() invoked.");
		
		if(this.em != null) this.em.clear();
		
		try { this.em.close(); } catch(Exception _ignored) {}
		try { this.emf.close();} catch(Exception _ignored) {}
	} // afterAll
	
	
//	@Disabled
	@Order(1)
	@Test
//	@RepeatedTest(1)
	@DisplayName("1. prepareData")
	@Timeout(value=1L, unit = TimeUnit.MINUTES)
	void prepareData() {
		log.trace("prepareData() invoked.");
		
		// -- 1 ------------------
		
		IntStream.rangeClosed(1, 7).forEachOrdered(seq -> {
			
			try { 
				this.em.getTransaction().begin();
				
				// 엔티티를 Persistence Context 에 저장 with em.persist method.
				Locker2 transientLocker = new Locker2();
				transientLocker.setName("NAME-"+seq);
				
				this.em.persist(transientLocker);
				
				this.em.getTransaction().commit();
			} catch(Exception e) {
				this.em.getTransaction().rollback();
				
				throw e;
			} // try-catch
		});	// .forEachOrdered
		

		// -- 2 ------------------
		
		IntStream.rangeClosed(1, 7).forEachOrdered(seq -> {
			
			try { 
				this.em.getTransaction().begin();
				
				// 엔티티를 Persistence Context 에 저장 with em.persist method.
				Student2 transientStudent = new Student2();
				transientStudent.setName("NAME-"+seq);
				
				Locker2 foundLocker = 
					this.em.<Locker2>find(Locker2.class, Long.valueOf(seq));
				Objects.requireNonNull(foundLocker);
				
				transientStudent.setLocker(foundLocker);
				
				this.em.persist(transientStudent);
				
				this.em.getTransaction().commit();
			} catch(Exception e) {
				this.em.getTransaction().rollback();
				
				throw e;
			} // try-catch
		});	// .forEachOrdered
		
	} // prepareData
	
	
//	@Disabled
	@Order(2)
	@Test
//	@RepeatedTest(1)
	@DisplayName("2. testO2OBiObjectGraphTraverseFromStudentToLocker")
	@Timeout(value=1L, unit = TimeUnit.MINUTES)
	void testO2OBiObjectGraphTraverseFromStudentToLocker() {
		log.trace("testO2OBiObjectGraphTraverseFromStudentToLocker() invoked.");
		
		// 모든 학생들을 조회해서, 각 학생의 학번과 사물함 번호를 출력함으로써,
		// JPA를 이용한 1:1, 단방향 연관관계대로 조회가 가능한지를 테스트 해보자!
		IntStream.rangeClosed(1, 7).forEachOrdered(seq -> {
			Student2 foundStudent = 
				this.em.<Student2>find(Student2.class, Long.valueOf(seq));
			
			assertNotNull(foundStudent);
			log.info("\t+ Student: id({}), locker({})", 
					foundStudent.getId(), foundStudent.getLocker().getId());
		}); // forEachOrdered
	} // testO2OBiObjectGraphTraverseFromStudentToLocker
	
	
	

} // end class
