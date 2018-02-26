package org.sitoolkit.util.crudanalyzer.domain.crud;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.sitoolkit.util.crudanalyzer.domain.methodcall.MethodCallDictionary;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CrudMatrixProcessor {

	/**
	 * 
	 * @param oldMap
	 *            key:repository function ex) XxxRepository.selectXxx
	 * @param dictionary
	 * @return newMap key:repository method signature ex)
	 *         a.b.c.XxxRepository.selectXxx(a.b.c.XxxEntity)
	 */
	public Map<String, CrudRow> function2signature(Map<String, CrudRow> oldMap, MethodCallDictionary dictionary) {
		Map<String, CrudRow> newMap = new HashMap<>();
		dictionary.getClassDefs().stream().filter(classDef -> classDef.getName().endsWith("Repository"))
				.forEach(repoDef -> {

					repoDef.getMethods().stream().forEach(repositoryMethod -> {
						String repositoryFunction = repoDef.getName() + "." + repositoryMethod.getName();
						CrudRow crudRow = oldMap.get(repositoryFunction);

						if (crudRow == null) {
							return;
						}

						crudRow.getRepositoryFunctions().add(repositoryFunction);
						newMap.put(repositoryMethod.getSignature(), crudRow);
					});
				});
		return newMap;
	}

	/**
	 * 
	 * @param repositoryMethodMap
	 *            key:repository method signature
	 * @param dictionary
	 * @return newMap key: controller method signature
	 */
	public Map<String, CrudRow> repository2controller(Map<String, CrudRow> repositoryMethodMap, MethodCallDictionary dictionary) {

		Map<String, CrudRow> controllerMethodMap = new TreeMap<>();

		dictionary.getClassDefs().stream().filter(classDef -> classDef.getName().endsWith("Controller"))
				.forEach(controllerDef -> {
					controllerDef.getMethods().stream().filter(controllerMethod -> controllerMethod.isPublic())
							.forEach(controllerMethod -> {

								controllerMethod.getMethodCallsRecursively().forEach(methodCalledByController -> {
									CrudRow repositoryMethodCrudRow = repositoryMethodMap.get(methodCalledByController.getSignature());
									if (repositoryMethodCrudRow != null) {
										CrudRow controllerMethodCrudRow = controllerMethodMap.get(controllerMethod.getSignature());

										if (controllerMethodCrudRow == null) {
											controllerMethodCrudRow = new CrudRow(controllerMethod.getSignature());
											controllerMethodMap.put(controllerMethod.getSignature(), controllerMethodCrudRow);
										}
										controllerMethodCrudRow.merge(repositoryMethodCrudRow);
										log.debug("Mapped {} -> {} : {}", controllerMethod.getSignature(),
												methodCalledByController.getSignature(), controllerMethodCrudRow.getCellMap());
									}
								});
							});
				});

		return controllerMethodMap;

	}
}
